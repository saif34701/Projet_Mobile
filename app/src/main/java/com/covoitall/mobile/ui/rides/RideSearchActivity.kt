package com.covoitall.mobile.ui.rides

import android.app.DatePickerDialog
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
import com.covoitall.mobile.data.remote.RideDto
import com.covoitall.mobile.databinding.ActivityRideSearchBinding
import com.covoitall.mobile.databinding.DialogReserveBinding
import com.covoitall.mobile.databinding.ItemRideBinding
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showMessage
import com.covoitall.mobile.ui.common.value
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class RideSearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRideSearchBinding
    private val viewModel: RideSearchViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideSearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = RideAdapter(::showReserveDialog)
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.resultsRecycler.layoutManager = LinearLayoutManager(this)
        binding.resultsRecycler.adapter = adapter
        binding.dateButton.setOnClickListener { pickDate() }
        binding.clearDateButton.setOnClickListener { viewModel.setDate(null) }
        binding.searchButton.setOnClickListener { search() }
        binding.sortGroup.setOnCheckedStateChangeListener { _, _ -> search() }

        viewModel.date.observe(this) { date ->
            binding.dateButton.text = date?.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRANCE))?.replaceFirstChar { it.uppercase() } ?: "Toutes les dates"
            binding.clearDateButton.isVisible = date != null
        }
        viewModel.results.observe(this) { state ->
            binding.loading.isVisible = state is UiState.Loading
            val rides = (state as? UiState.Success)?.data.orEmpty()
            adapter.submitList(rides)
            binding.resultsTitle.text = when (state) {
                is UiState.Success -> "${rides.size} trajet(s) disponible(s)"
                is UiState.Error -> "Recherche impossible"
                else -> ""
            }
            binding.emptyText.isVisible = state is UiState.Error || (state is UiState.Success && rides.isEmpty())
            binding.emptyText.text = (state as? UiState.Error)?.message
                ?: "Aucun trajet ne correspond à vos critères.\nEssayez une autre date ou augmentez le prix maximum."
        }
        viewModel.reserving.observe(this) { binding.searchButton.isEnabled = !it }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }
    }

    private fun search() {
        val sort = when (binding.sortGroup.checkedChipId) {
            R.id.sortPrice -> "price"
            R.id.sortRating -> "rating"
            else -> "departure"
        }
        viewModel.search(binding.originInput.value(), binding.destinationInput.value(), binding.seatsInput.value(), binding.maxPriceInput.value(), sort)
    }

    private fun pickDate() {
        val initial = viewModel.date.value ?: LocalDate.now()
        DatePickerDialog(this, { _, year, month, day -> viewModel.setDate(LocalDate.of(year, month + 1, day)) },
            initial.year, initial.monthValue - 1, initial.dayOfMonth
        ).apply { datePicker.minDate = System.currentTimeMillis() - 1000 }.show()
    }

    private fun showReserveDialog(ride: RideDto) {
        val dialogBinding = DialogReserveBinding.inflate(layoutInflater)
        var seats = viewModel.defaultSeats(ride)
        fun refresh() {
            dialogBinding.seatsValue.text = seats.toString()
            dialogBinding.totalText.text = viewModel.totalPrice(ride, seats)
            dialogBinding.minusButton.isEnabled = seats > 1
            dialogBinding.plusButton.isEnabled = seats < ride.seatsAvailable
        }
        dialogBinding.summaryText.text = "${Formatters.dateTime(ride.departureAt)}\n${ride.driverName} · ${Formatters.price(ride.price)} par place · ${ride.seatsAvailable} place(s) restante(s)"
        dialogBinding.modeText.text = if (ride.acceptanceMode == "automatic") "Confirmation immédiate : le conducteur accepte automatiquement les réservations."
        else "Le conducteur doit valider votre demande. Le paiement se fait ensuite depuis le site web."
        dialogBinding.minusButton.setOnClickListener { seats--; refresh() }
        dialogBinding.plusButton.setOnClickListener { seats++; refresh() }
        refresh()
        MaterialAlertDialogBuilder(this)
            .setTitle("${ride.origin} → ${ride.destination}")
            .setView(dialogBinding.root)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Confirmer") { _, _ -> viewModel.reserve(ride, seats) }
            .show()
    }
}

private class RideAdapter(private val onReserve: (RideDto) -> Unit) : ListAdapter<RideDto, RideAdapter.Holder>(Diff) {
    class Holder(val binding: ItemRideBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(ItemRideBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val ride = getItem(position)
        with(holder.binding) {
            typeText.text = if (ride.rideType == "regulier") "Régulier · ${ride.recurrenceDays.joinToString(", ") { it.take(3) }}" else "Ponctuel"
            routeText.text = "${ride.origin} → ${ride.destination}"
            priceText.text = Formatters.price(ride.price)
            departureText.text = Formatters.dateTime(ride.departureAt)
            val rating = if (ride.driverRating > 0) "★ ${ride.driverRating}" else "Nouveau"
            driverText.text = listOfNotNull(ride.driverName, rating, ride.vehicle?.model).joinToString(" · ")
            seatsText.text = "${ride.seatsAvailable} place(s)"
            modeText.text = if (ride.acceptanceMode == "automatic") "Confirmation immédiate" else "Validation par le conducteur"
            reserveButton.setOnClickListener { onReserve(ride) }
        }
    }

    object Diff : DiffUtil.ItemCallback<RideDto>() {
        override fun areItemsTheSame(oldItem: RideDto, newItem: RideDto) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: RideDto, newItem: RideDto) = oldItem == newItem
    }
}
