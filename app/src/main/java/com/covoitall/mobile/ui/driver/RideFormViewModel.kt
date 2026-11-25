package com.covoitall.mobile.ui.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.data.remote.RideRequest
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.data.repository.RideRepository
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.UiState
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class RideField { ORIGIN, DESTINATION, DEPARTURE, SEATS, PRICE, DAYS }

/** Publication et modification d'un trajet, avec les règles de gestion du covoiturage. */
class RideFormViewModel(private val repository: RideRepository) : ViewModel() {

    private val _ride = MutableLiveData<RideEntity?>()
    /** Trajet en cours de modification (null pour une publication). */
    val ride: LiveData<RideEntity?> = _ride

    private val _departure = MutableLiveData<LocalDateTime?>(null)
    private val _departureLabel = MutableLiveData("Choisir la date et l'heure")
    val departureLabel: LiveData<String> = _departureLabel

    private val _fieldErrors = MutableLiveData<Map<RideField, String>>(emptyMap())
    val fieldErrors: LiveData<Map<RideField, String>> = _fieldErrors

    private val _estimate = MutableLiveData("")
    val estimate: LiveData<String> = _estimate

    private val _saveState = MutableLiveData<UiState<Boolean>>(UiState.Idle)
    /** Succès : true si c'était une publication, false pour une modification. */
    val saveState: LiveData<UiState<Boolean>> = _saveState

    private var loaded = false

    fun load(rideId: String?) {
        if (loaded) return
        loaded = true
        if (rideId == null) {
            // Par défaut : demain à 8 h.
            setDeparture(LocalDate.now().plusDays(1).atTime(8, 0))
            return
        }
        viewModelScope.launch {
            val ride = repository.ride(rideId)
            _ride.value = ride
            ride?.let { Formatters.parse(it.departureAt)?.let(::setDeparture) }
        }
    }

    fun currentDeparture(): LocalDateTime = _departure.value ?: LocalDateTime.now().plusHours(1)

    fun setDate(date: LocalDate) = setDeparture(date.atTime(currentDeparture().toLocalTime()))

    fun setTime(time: LocalTime) = setDeparture(currentDeparture().toLocalDate().atTime(time))

    private fun setDeparture(value: LocalDateTime) {
        _departure.value = value.withSecond(0).withNano(0)
        _departureLabel.value = value.format(LABEL).replaceFirstChar { it.uppercase() }
    }

    /** Revenu maximal si toutes les places sont réservées (mis à jour pendant la saisie). */
    fun updateEstimate(seatsText: String, priceText: String) {
        val seats = seatsText.toIntOrNull()
        val price = priceText.replace(',', '.').toDoubleOrNull()
        _estimate.value = if (seats != null && price != null && seats in 1..8 && price > 0)
            "Revenu maximal : ${Formatters.price(seats * price)} pour $seats place(s)" else ""
    }

    fun save(origin: String, destination: String, seatsText: String, priceText: String, regular: Boolean, days: List<String>, automatic: Boolean) {
        val seats = seatsText.toIntOrNull()
        val price = priceText.replace(',', '.').toDoubleOrNull()
        val departure = _departure.value
        val reserved = _ride.value?.seatsReserved ?: 0
        val errors = buildMap {
            if (origin.trim().length < 2) put(RideField.ORIGIN, "Indiquez le point de départ")
            if (destination.trim().length < 2) put(RideField.DESTINATION, "Indiquez le point d'arrivée")
            else if (origin.trim().equals(destination.trim(), ignoreCase = true)) put(RideField.DESTINATION, "L'arrivée doit différer du départ")
            when {
                departure == null -> put(RideField.DEPARTURE, "Choisissez la date et l'heure de départ")
                departure.isBefore(LocalDateTime.now().plusMinutes(15)) -> put(RideField.DEPARTURE, "Le départ doit être prévu au moins 15 minutes à l'avance")
                departure.isAfter(LocalDateTime.now().plusMonths(3)) -> put(RideField.DEPARTURE, "Publication limitée aux 3 prochains mois")
            }
            when {
                seats == null || seats !in 1..8 -> put(RideField.SEATS, "Entre 1 et 8 places")
                seats < reserved -> put(RideField.SEATS, "$reserved place(s) déjà réservée(s)")
            }
            if (price == null || price < 0.5 || price > 500) put(RideField.PRICE, "Prix entre 0,5 et 500 TND")
            if (regular && days.isEmpty()) put(RideField.DAYS, "Choisissez au moins un jour de circulation")
        }
        _fieldErrors.value = errors
        if (errors.isNotEmpty()) return

        val request = RideRequest(
            origin = origin.trim(),
            destination = destination.trim(),
            departureAt = departure!!.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
            seatsTotal = seats!!,
            price = price!!,
            rideType = if (regular) "regulier" else "ponctuel",
            recurrenceDays = if (regular) days else emptyList(),
            acceptanceMode = if (automatic) "automatic" else "manual"
        )
        val editing = _ride.value
        _saveState.value = UiState.Loading
        viewModelScope.launch {
            val result = if (editing == null) repository.publish(request) else repository.update(editing.id, request)
            _saveState.value = when (result) {
                is Resource.Success -> UiState.Success(editing == null)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
    }

    companion object {
        val DAYS = listOf("Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche")
        private val LABEL = DateTimeFormatter.ofPattern("EEEE d MMMM yyyy 'à' HH:mm", Locale.FRANCE)
    }
}
