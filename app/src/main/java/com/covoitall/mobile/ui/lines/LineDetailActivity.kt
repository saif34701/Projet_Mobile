package com.covoitall.mobile.ui.lines

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.covoitall.mobile.R
import com.covoitall.mobile.data.remote.LiveLineDto
import com.covoitall.mobile.databinding.ActivityLineDetailBinding
import com.covoitall.mobile.databinding.ItemRowBinding
import com.covoitall.mobile.ui.common.StatusKind
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showError
import com.covoitall.mobile.ui.common.statusStyle
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

/** Détail d'une ligne : arrêts, horaires et suivi GPS de la navette sur une carte OpenStreetMap. */
class LineDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLineDetailBinding
    private val viewModel: LineDetailViewModel by appViewModels()
    private var stopIds: List<String> = emptyList()
    private var vehicleMarker: Marker? = null
    private var mapFitted = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLineDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        binding.mapView.apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            controller.setZoom(12.0)
            controller.setCenter(GeoPoint(36.8065, 10.1815))
            // La carte capte les gestes au lieu du défilement de la page.
            setOnTouchListener { view, _ -> view.parent.requestDisallowInterceptTouchEvent(true); false }
        }
        binding.stopDropdown.setOnItemClickListener { _, _, position, _ -> viewModel.selectStop(stopIds.getOrNull(position)) }

        viewModel.setLine(intent.getStringExtra(EXTRA_LINE_ID) ?: return finish())

        viewModel.detail.observe(this) { detail ->
            if (detail == null) return@observe
            binding.toolbar.title = "Ligne ${detail.line.code} · ${detail.line.name}"
            binding.routeText.text = detail.stops.joinToString(" → ") { it.name }
            binding.nextDepartureText.text = "Prochain départ : ${detail.nextDeparture}"
            stopIds = detail.stops.map { it.id }
            binding.stopDropdown.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, detail.stops.map { it.name }))
            renderSchedules(detail)
            if (viewModel.live.value == null) {
                binding.stopsContainer.removeAllViews()
                detail.stops.forEachIndexed { index, stop -> addRow(binding.stopsContainer, "${index + 1}", stop.name, stop.address, "") }
            }
            drawRoute(detail.stops.map { GeoPoint(it.latitude, it.longitude) }, detail.stops.map { it.name })
        }
        viewModel.live.observe(this) { live -> live?.let(::renderLive) }
        viewModel.liveError.observe(this) { binding.liveErrorText.showError(it) }
        viewModel.waitEstimate.observe(this) { estimate ->
            binding.waitTitle.text = estimate.title
            binding.waitTitle.setTextColor(ContextCompat.getColor(this, if (estimate.highlight) R.color.green_dark else R.color.ink))
            binding.waitDetail.text = estimate.detail
            binding.waitDetail.isVisible = estimate.detail.isNotEmpty()
        }
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        viewModel.startTracking()
    }

    override fun onPause() {
        viewModel.stopTracking()
        binding.mapView.onPause()
        super.onPause()
    }

    private fun renderSchedules(detail: LineDetail) {
        binding.schedulesContainer.removeAllViews()
        if (detail.schedules.isEmpty()) {
            addRow(binding.schedulesContainer, "–", "Aucun horaire publié", "", "")
            return
        }
        detail.schedules.forEach { schedule ->
            addRow(binding.schedulesContainer, "🕒", "${schedule.departureTime} → ${schedule.arrivalTime}", schedule.days.joinToString(", "), "")
        }
    }

    private fun renderLive(live: LiveLineDto) {
        binding.liveStatusText.text = if (live.active) "● En direct" else "Hors service"
        binding.liveStatusText.statusStyle(if (live.active) StatusKind.SUCCESS else StatusKind.WARNING)
        binding.vehicleText.text = when {
            !live.active -> "Aucune navette en service sur cette ligne actuellement."
            live.position == null -> "Navette en service, en attente de sa position GPS."
            else -> listOfNotNull(
                live.vehicle?.let { "${it.model} (${it.registration})" },
                live.nextStop?.let { "Prochain arrêt : $it" },
                if (live.positionStale) "Signal GPS perdu" else null
            ).joinToString(" · ")
        }
        val incident = live.incidents.firstOrNull()
        binding.delayText.showError(
            if (live.delayMinutes > 0) "Retard estimé : ${live.delayMinutes} min" + (incident?.let { " · ${it.type}${if (it.description.isNotBlank()) " : ${it.description}" else ""}" } ?: "")
            else null
        )
        // Liste des arrêts avec l'estimation de passage.
        binding.stopsContainer.removeAllViews()
        live.stops.forEachIndexed { index, stop ->
            val value = when {
                stop.passed -> "Desservi"
                stop.etaMinutes != null -> "~ ${stop.etaMinutes} min"
                else -> ""
            }
            addRow(binding.stopsContainer, "${index + 1}", stop.name, if (stop.name == live.nextStop) "Prochain arrêt" else "", value, dimmed = stop.passed)
        }
        // Position de la navette sur la carte.
        val position = live.position
        if (position == null) {
            vehicleMarker?.let { binding.mapView.overlays.remove(it) }
            vehicleMarker = null
        } else {
            val point = GeoPoint(position.latitude, position.longitude)
            val marker = vehicleMarker ?: Marker(binding.mapView).also {
                it.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                it.icon = ContextCompat.getDrawable(this, R.drawable.ic_bus)
                binding.mapView.overlays.add(it)
                vehicleMarker = it
            }
            marker.position = point
            marker.title = "Navette ligne ${live.lineCode}"
            marker.snippet = live.nextStop?.let { "Prochain arrêt : $it" }
        }
        binding.mapView.invalidate()
    }

    private fun drawRoute(points: List<GeoPoint>, names: List<String>) {
        val map = binding.mapView
        map.overlays.removeAll { it is Polyline || (it is Marker && it != vehicleMarker) }
        if (points.size > 1) {
            map.overlays.add(0, Polyline(map).apply {
                setPoints(points)
                outlinePaint.color = ContextCompat.getColor(this@LineDetailActivity, R.color.green)
                outlinePaint.strokeWidth = 9f
            })
        }
        points.forEachIndexed { index, point ->
            map.overlays.add(Marker(map).apply {
                position = point
                title = "${index + 1}. ${names[index]}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            })
        }
        if (!mapFitted && points.isNotEmpty()) {
            mapFitted = true
            map.post {
                if (points.size == 1) map.controller.setCenter(points.first())
                else map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points).increaseByScale(1.4f), false)
            }
        }
        map.invalidate()
    }

    private fun addRow(container: android.widget.LinearLayout, index: String, title: String, subtitle: String, value: String, dimmed: Boolean = false) {
        val row = ItemRowBinding.inflate(layoutInflater, container, false)
        row.indexText.text = index
        row.titleText.text = title
        row.subtitleText.text = subtitle
        row.subtitleText.isVisible = subtitle.isNotEmpty()
        row.valueText.text = value
        if (dimmed) row.titleText.setTextColor(Color.GRAY)
        container.addView(row.root)
    }

    companion object {
        const val EXTRA_LINE_ID = "line_id"
    }
}
