package com.covoitall.mobile.ui.rides

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
import com.covoitall.mobile.data.local.ReservationEntity
import com.covoitall.mobile.databinding.ActivityMyReservationsBinding
import com.covoitall.mobile.databinding.ItemReservationBinding
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showMessage
import com.covoitall.mobile.ui.common.statusKind
import com.covoitall.mobile.ui.common.statusStyle
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MyReservationsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMyReservationsBinding
    private val viewModel: MyReservationsViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyReservationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = ReservationAdapter(viewModel, ::confirmCancel, ::pickSeats)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter
        binding.swipeRefresh.setOnRefreshListener { viewModel.refresh() }
        binding.filterGroup.setOnCheckedStateChangeListener { _, ids -> viewModel.showHistory(ids.firstOrNull() == R.id.historyChip) }

        viewModel.reservations.observe(this) { list ->
            adapter.submitList(list)
            binding.emptyText.isVisible = list.isEmpty()
        }
        viewModel.summary.observe(this) { summary ->
            binding.upcomingText.text = "${summary.upcoming}\nà venir"
            binding.toPayText.text = "${summary.toPay}\nà payer"
            binding.spentText.text = "${summary.spent}\npayé"
        }
        viewModel.loading.observe(this) { binding.swipeRefresh.isRefreshing = it }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }
    }

    private fun confirmCancel(item: ReservationEntity) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Annuler la réservation ?")
            .setMessage("${item.origin} → ${item.destination}\n${Formatters.dateTime(item.departureAt)}")
            .setNegativeButton("Non", null)
            .setPositiveButton("Annuler la réservation") { _, _ -> viewModel.cancel(item) }
            .show()
    }

    private fun pickSeats(item: ReservationEntity) {
        val options = (1..viewModel.maxSeats(item)).toList()
        val labels = options.map { "$it place(s) · ${Formatters.price(it * item.pricePerSeat)}" }.toTypedArray()
        MaterialAlertDialogBuilder(this)
            .setTitle("Nombre de places")
            .setSingleChoiceItems(labels, options.indexOf(item.seats)) { dialog, index ->
                viewModel.changeSeats(item, options[index])
                dialog.dismiss()
            }
            .setNegativeButton("Fermer", null)
            .show()
    }
}

private class ReservationAdapter(
    private val viewModel: MyReservationsViewModel,
    private val onCancel: (ReservationEntity) -> Unit,
    private val onSeats: (ReservationEntity) -> Unit
) : ListAdapter<ReservationEntity, ReservationAdapter.Holder>(Diff) {
    class Holder(val binding: ItemReservationBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemReservationBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val item = getItem(position)
        with(holder.binding) {
            routeText.text = "${item.origin} → ${item.destination}"
            statusText.text = Formatters.reservationStatus(item.status)
            statusText.statusStyle(statusKind(item.status))
            departureText.text = Formatters.dateTime(item.departureAt)
            val rideInfo = if (item.rideStatus != "scheduled") " · Trajet ${Formatters.rideStatus(item.rideStatus).lowercase()}" else ""
            detailText.text = "${item.driverName} · ${item.seats} place(s) · ${Formatters.price(item.amount)}$rideInfo"
            val modifiable = viewModel.canModify(item)
            seatsButton.isVisible = modifiable
            cancelButton.isVisible = modifiable
            deleteButton.isVisible = viewModel.canDelete(item)
            actions.isVisible = modifiable || deleteButton.isVisible
            seatsButton.setOnClickListener { onSeats(item) }
            cancelButton.setOnClickListener { onCancel(item) }
            deleteButton.setOnClickListener { viewModel.delete(item) }
        }
    }

    object Diff : DiffUtil.ItemCallback<ReservationEntity>() {
        override fun areItemsTheSame(oldItem: ReservationEntity, newItem: ReservationEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: ReservationEntity, newItem: ReservationEntity) = oldItem == newItem
    }
}
