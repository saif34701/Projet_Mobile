package com.covoitall.mobile.ui.driver

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.covoitall.mobile.data.remote.ReservationDto
import com.covoitall.mobile.databinding.ActivityRideReservationsBinding
import com.covoitall.mobile.databinding.ItemRideReservationBinding
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showMessage
import com.covoitall.mobile.ui.common.statusKind
import com.covoitall.mobile.ui.common.statusStyle

/** Demandes de réservation reçues sur un trajet publié. */
class RideReservationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRideReservationsBinding
    private val viewModel: RideReservationsViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RideReservationAdapter(viewModel)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }

        viewModel.ride.observe(this) { ride ->
            if (ride == null) return@observe
            binding.rideText.text = "${ride.origin} → ${ride.destination}\n${Formatters.dateTime(ride.departureAt)}"
            binding.seatsText.text = "${ride.seatsReserved}/${ride.seatsTotal} places réservées · ${ride.seatsAvailable} disponible(s) · ${Formatters.price(ride.price)} / place"
        }
        viewModel.reservations.observe(this) { state ->
            binding.swipeRefresh.isRefreshing = false
            binding.loading.isVisible = state is UiState.Loading && adapter.itemCount == 0
            val list = (state as? UiState.Success)?.data
            if (list != null) adapter.submitList(list)
            binding.emptyText.isVisible = state is UiState.Error || list?.isEmpty() == true
            binding.emptyText.text = (state as? UiState.Error)?.message ?: "Aucune réservation pour ce trajet."
        }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }

        viewModel.load(intent.getStringExtra(EXTRA_RIDE_ID) ?: return finish())
    }

    companion object {
        const val EXTRA_RIDE_ID = "ride_id"
    }
}

private class RideReservationAdapter(private val viewModel: RideReservationsViewModel) :
    ListAdapter<ReservationDto, RideReservationAdapter.Holder>(Diff) {
    class Holder(val binding: ItemRideReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemRideReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            nameText.text = item.passengerName
            statusText.text = Formatters.reservationStatus(item.status)
            statusText.statusStyle(statusKind(item.status))
            detailText.text = "${item.seats} place(s) · ${Formatters.price(item.amount)} · ${item.passengerPhone}\nDemande du ${Formatters.utcDateTime(item.createdAt)}"
            actions.isVisible = item.status == "pending"
            acceptButton.isEnabled = viewModel.canAccept(item)
            acceptButton.setOnClickListener { viewModel.accept(item) }
            refuseButton.setOnClickListener { viewModel.refuse(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<ReservationDto>() {
        override fun areItemsTheSame(oldItem: ReservationDto, newItem: ReservationDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ReservationDto, newItem: ReservationDto) = oldItem == newItem
    }
}
