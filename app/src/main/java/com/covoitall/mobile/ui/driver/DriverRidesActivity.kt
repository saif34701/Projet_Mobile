package com.covoitall.mobile.ui.driver

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.covoitall.mobile.R
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.databinding.ActivityDriverRidesBinding
import com.covoitall.mobile.databinding.ItemDriverRideBinding
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showMessage
import com.covoitall.mobile.ui.common.statusKind
import com.covoitall.mobile.ui.common.statusStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Gestion des trajets publiés par le conducteur. */
class DriverRidesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDriverRidesBinding
    private val viewModel: DriverRidesViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDriverRidesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = DriverRideAdapter(viewModel, object : RideActions {
            override fun edit(ride: RideEntity) = openForm(ride.id)
            override fun reservations(ride: RideEntity) {
                startActivity(Intent(this@DriverRidesActivity, RideReservationsActivity::class.java).putExtra(RideReservationsActivity.EXTRA_RIDE_ID, ride.id))
            }
            override fun delete(ride: RideEntity) {
                MaterialAlertDialogBuilder(this@DriverRidesActivity)
                    .setTitle("${ride.origin} → ${ride.destination}")
                    .setMessage(viewModel.deleteWarning(ride))
                    .setNegativeButton("Annuler", null)
                    .setPositiveButton("Supprimer") { _, _ -> viewModel.delete(ride) }
                    .show()
            }
            override fun status(ride: RideEntity) {
                if (viewModel.canComplete(ride)) viewModel.markCompleted(ride) else viewModel.markArrived(ride)
            }
        })
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.publishButton.setOnClickListener { openForm(null) }
        binding.filterGroup.setOnCheckedStateChangeListener { _, ids -> viewModel.showHistory(ids.firstOrNull() == R.id.historyChip) }

        viewModel.user.observe(this) { user ->
            val approved = user?.isApprovedDriver == true
            binding.approvalText.isVisible = !approved
            binding.publishButton.isVisible = approved
        }
        viewModel.rides.observe(this) { rides ->
            adapter.submitList(rides)
            binding.emptyText.isVisible = rides.isEmpty()
        }
        viewModel.stats.observe(this) { stats ->
            binding.upcomingStat.text = "${stats.upcoming}\nà venir"
            binding.fillStat.text = "${stats.fillRate} %\nremplissage"
            binding.revenueStat.text = "${stats.expectedRevenue}\nattendus"
            binding.pendingStat.text = "${stats.pendingRequests}\ndemande(s)"
        }
        viewModel.loading.observe(this) { binding.swipeRefresh.isRefreshing = it }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refresh()
    }

    private fun openForm(rideId: String?) {
        startActivity(Intent(this, RideFormActivity::class.java).putExtra(RideFormActivity.EXTRA_RIDE_ID, rideId))
    }
}

interface RideActions {
    fun edit(ride: RideEntity)
    fun reservations(ride: RideEntity)
    fun delete(ride: RideEntity)
    fun status(ride: RideEntity)
}

private class DriverRideAdapter(
    private val viewModel: DriverRidesViewModel,
    private val actions: RideActions
) : ListAdapter<RideEntity, DriverRideAdapter.Holder>(Diff) {
    class Holder(val binding: ItemDriverRideBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemDriverRideBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ride = getItem(position)
        with(holder.binding) {
            val mode = if (ride.acceptanceMode == "automatic") "acceptation auto" else "acceptation manuelle"
            typeText.text = if (ride.rideType == "regulier") "Régulier · ${ride.recurrenceDays.joinToString(", ") { it.take(3) }} · $mode" else "Ponctuel · $mode"
            statusText.text = Formatters.rideStatus(ride.status)
            statusText.statusStyle(statusKind(ride.status))
            routeText.text = "${ride.origin} → ${ride.destination}"
            departureText.text = Formatters.dateTime(ride.departureAt)
            val pending = if (ride.pendingRequests > 0) " · ${ride.pendingRequests} en attente" else ""
            detailText.text = "${ride.seatsReserved}/${ride.seatsTotal} places réservées · ${Formatters.price(ride.price)} / place$pending"
            occupancyBar.progress = if (ride.seatsTotal == 0) 0 else ride.seatsReserved * 100 / ride.seatsTotal
            val canArrive = viewModel.canMarkArrived(ride)
            val canComplete = viewModel.canComplete(ride)
            statusButton.isVisible = canArrive || canComplete
            statusButton.text = if (canComplete) "Trajet terminé" else "Arrivé au point de rendez-vous"
            statusButton.setOnClickListener { actions.status(ride) }
            editButton.isVisible = viewModel.canEdit(ride)
            deleteButton.isVisible = viewModel.canEdit(ride)
            reservationsButton.text = if (ride.pendingRequests > 0) "Réservations (${ride.pendingRequests})" else "Réservations"
            reservationsButton.setOnClickListener { actions.reservations(ride) }
            editButton.setOnClickListener { actions.edit(ride) }
            deleteButton.setOnClickListener { actions.delete(ride) }
        }
    }

    object Diff : DiffUtil.ItemCallback<RideEntity>() {
        override fun areItemsTheSame(oldItem: RideEntity, newItem: RideEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RideEntity, newItem: RideEntity) = oldItem == newItem
    }
}
