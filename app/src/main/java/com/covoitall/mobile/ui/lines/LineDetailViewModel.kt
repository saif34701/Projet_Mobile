package com.covoitall.mobile.ui.lines

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.LineEntity
import com.covoitall.mobile.data.local.ScheduleEntity
import com.covoitall.mobile.data.local.StopEntity
import com.covoitall.mobile.data.remote.LiveLineDto
import com.covoitall.mobile.data.repository.LineRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.ui.common.Formatters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LineDetail(
    val line: LineEntity,
    val stops: List<StopEntity>,
    val schedules: List<ScheduleEntity>,
    val nextDeparture: String
)

/** Synthèse affichée pour l'arrêt choisi par le passager. */
data class WaitEstimate(val title: String, val detail: String, val highlight: Boolean)

class LineDetailViewModel(private val repository: LineRepository) : ViewModel() {
    private val lineId = MutableStateFlow<String?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val detail: LiveData<LineDetail?> = lineId.filterNotNull().flatMapLatest { id ->
        combine(repository.line(id), repository.stops, repository.schedulesForLine(id)) { line, stops, schedules ->
            line?.let {
                val byId = stops.associateBy { stop -> stop.id }
                LineDetail(it, it.stopIds.mapNotNull { stopId -> byId[stopId] }, schedules, DepartureCalculator.next(schedules))
            }
        }
    }.asLiveData()

    private val _live = MutableLiveData<LiveLineDto?>()
    val live: LiveData<LiveLineDto?> = _live

    private val _liveError = MutableLiveData<String?>()
    val liveError: LiveData<String?> = _liveError

    private val selectedStopId = MutableLiveData<String?>()

    /** Temps d'attente recalculé à chaque mise à jour de la position ou changement d'arrêt. */
    val waitEstimate: LiveData<WaitEstimate> = MediatorLiveData<WaitEstimate>().apply {
        val update = { value = estimate(_live.value, selectedStopId.value) }
        addSource(_live) { update() }
        addSource(selectedStopId) { update() }
    }

    private var trackingJob: Job? = null

    fun setLine(id: String) {
        if (lineId.value == id) return
        lineId.value = id
        _live.value = null
    }

    fun selectStop(stopId: String?) {
        selectedStopId.value = stopId
    }

    /** Démarre l'actualisation périodique de la position (appelé quand l'écran devient visible). */
    fun startTracking() {
        val id = lineId.value ?: return
        if (trackingJob?.isActive == true) return
        trackingJob = viewModelScope.launch {
            while (isActive) {
                when (val result = repository.liveStatus(id)) {
                    is Resource.Success -> {
                        _live.value = result.data
                        _liveError.value = null
                    }
                    is Resource.Error -> _liveError.value =
                        if (result.code == 401) "Connectez-vous pour suivre la navette en temps réel." else result.message
                }
                delay(REFRESH_INTERVAL_MS)
            }
        }
    }

    fun stopTracking() {
        trackingJob?.cancel()
        trackingJob = null
    }

    private fun estimate(live: LiveLineDto?, stopId: String?): WaitEstimate {
        if (live == null) return WaitEstimate("Suivi en cours de connexion…", "", false)
        if (!live.active) {
            val next = live.nextDeparture
            val text = if (next == null) "Aucun départ programmé" else
                "Prochain départ théorique : ${if (next.inDays == 0) "aujourd'hui" else next.day.lowercase()} à ${next.departureTime}"
            return WaitEstimate("Aucune navette en service", text, false)
        }
        if (live.position == null) return WaitEstimate("Navette en service", "En attente de la première position GPS.", false)
        val stop = live.stops.firstOrNull { it.id == stopId }
            ?: return WaitEstimate("Navette en service", "Choisissez votre arrêt pour estimer votre attente.", false)
        val delayInfo = if (live.delayMinutes > 0) " (dont ${live.delayMinutes} min de retard signalé)" else ""
        return when {
            stop.passed -> WaitEstimate("Arrêt déjà desservi", "La navette a dépassé ${stop.name}. Consultez le prochain départ.", false)
            stop.etaMinutes != null -> WaitEstimate(
                Formatters.minutes(stop.etaMinutes),
                "Attente estimée à ${stop.name}$delayInfo${if (live.positionStale) " · signal GPS perdu, dernière position connue" else ""}",
                true
            )
            else -> WaitEstimate("Estimation indisponible", "Consultez les horaires théoriques.", false)
        }
    }

    override fun onCleared() {
        stopTracking()
    }

    companion object {
        private const val REFRESH_INTERVAL_MS = 5_000L
    }
}
