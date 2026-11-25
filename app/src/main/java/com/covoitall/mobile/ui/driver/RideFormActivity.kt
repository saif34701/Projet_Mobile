package com.covoitall.mobile.ui.driver

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.covoitall.mobile.databinding.ActivityRideFormBinding
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.onTextChanged
import com.covoitall.mobile.ui.common.showError
import com.covoitall.mobile.ui.common.value
import com.google.android.material.chip.Chip
import android.widget.Toast
import java.time.LocalDate
import java.time.LocalTime

/** Formulaire de publication (POST) ou de modification (PUT) d'un trajet. */
class RideFormActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRideFormBinding
    private val viewModel: RideFormViewModel by appViewModels()
    private val dayChips = mutableMapOf<String, Chip>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRideFormBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        RideFormViewModel.DAYS.forEach { day ->
            val chip = Chip(this).apply {
                text = day.take(3)
                isCheckable = true
            }
            dayChips[day] = chip
            binding.daysGroup.addView(chip)
        }
        binding.regularSwitch.setOnCheckedChangeListener { _, checked -> binding.daysGroup.isVisible = checked }
        binding.dateButton.setOnClickListener {
            val current = viewModel.currentDeparture()
            DatePickerDialog(this, { _, y, m, d -> viewModel.setDate(LocalDate.of(y, m + 1, d)) }, current.year, current.monthValue - 1, current.dayOfMonth)
                .apply { datePicker.minDate = System.currentTimeMillis() - 1000 }
                .show()
        }
        binding.timeButton.setOnClickListener {
            val current = viewModel.currentDeparture()
            TimePickerDialog(this, { _, h, min -> viewModel.setTime(LocalTime.of(h, min)) }, current.hour, current.minute, true).show()
        }
        binding.seatsInput.onTextChanged { viewModel.updateEstimate(it, binding.priceInput.value()) }
        binding.priceInput.onTextChanged { viewModel.updateEstimate(binding.seatsInput.value(), it) }
        binding.saveButton.setOnClickListener {
            viewModel.save(
                origin = binding.originInput.value(),
                destination = binding.destinationInput.value(),
                seatsText = binding.seatsInput.value(),
                priceText = binding.priceInput.value(),
                regular = binding.regularSwitch.isChecked,
                days = RideFormViewModel.DAYS.filter { dayChips[it]?.isChecked == true },
                automatic = binding.automaticSwitch.isChecked
            )
        }

        viewModel.ride.observe(this) { ride ->
            if (ride == null || savedInstanceState != null) return@observe
            binding.toolbar.title = "Modifier le trajet"
            binding.saveButton.text = "Enregistrer les modifications"
            binding.originInput.setText(ride.origin)
            binding.destinationInput.setText(ride.destination)
            binding.seatsInput.setText(ride.seatsTotal.toString())
            binding.priceInput.setText(Formatters.plainNumber(ride.price))
            binding.regularSwitch.isChecked = ride.rideType == "regulier"
            ride.recurrenceDays.forEach { dayChips[it]?.isChecked = true }
            binding.automaticSwitch.isChecked = ride.acceptanceMode == "automatic"
            // Après une réservation confirmée, le prix et l'itinéraire sont figés (règle serveur).
            val locked = ride.seatsReserved > 0
            binding.originInput.isEnabled = !locked
            binding.destinationInput.isEnabled = !locked
            binding.priceInput.isEnabled = !locked
        }
        viewModel.departureLabel.observe(this) { binding.departureText.text = it }
        viewModel.estimate.observe(this) { binding.estimateText.text = it }
        viewModel.fieldErrors.observe(this) { errors ->
            binding.originLayout.error = errors[RideField.ORIGIN]
            binding.destinationLayout.error = errors[RideField.DESTINATION]
            binding.seatsLayout.error = errors[RideField.SEATS]
            binding.priceLayout.error = errors[RideField.PRICE]
            binding.departureError.showError(errors[RideField.DEPARTURE])
            binding.daysError.showError(errors[RideField.DAYS])
        }
        viewModel.saveState.observe(this) { state ->
            binding.loading.isVisible = state is UiState.Loading
            binding.saveButton.isEnabled = state !is UiState.Loading
            binding.errorText.showError((state as? UiState.Error)?.message)
            if (state is UiState.Success) {
                Toast.makeText(this, if (state.data) "Trajet publié" else "Trajet modifié", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        viewModel.load(intent.getStringExtra(EXTRA_RIDE_ID))
        viewModel.updateEstimate(binding.seatsInput.value(), binding.priceInput.value())
    }

    companion object {
        const val EXTRA_RIDE_ID = "ride_id"
    }
}
