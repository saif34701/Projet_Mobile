package com.covoitall.mobile.ui.rides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.remote.RideDto
import com.covoitall.mobile.data.repository.ReservationRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.data.repository.RideRepository
import com.covoitall.mobile.data.repository.RideSearchFilters
import com.covoitall.mobile.ui.common.Event
import com.covoitall.mobile.ui.common.Formatters
import com.covoitall.mobile.ui.common.UiState
import kotlinx.coroutines.launch
import java.time.LocalDate

class RideSearchViewModel(
    private val rideRepository: RideRepository,
    private val reservationRepository: ReservationRepository
) : ViewModel() {

    private val _results = MutableLiveData<UiState<List<RideDto>>>(UiState.Idle)
    val results: LiveData<UiState<List<RideDto>>> = _results

    private val _date = MutableLiveData<LocalDate?>(null)
    val date: LiveData<LocalDate?> = _date

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    private val _reserving = MutableLiveData(false)
    val reserving: LiveData<Boolean> = _reserving

    private var lastFilters = RideSearchFilters()

    init {
        runSearch(lastFilters)
    }

    fun setDate(value: LocalDate?) {
        _date.value = value
    }

    fun search(origin: String, destination: String, seatsText: String, maxPriceText: String, sort: String) {
        val seats = seatsText.toIntOrNull()
        val maxPrice = maxPriceText.replace(',', '.').toDoubleOrNull()
        val error = when {
            seats == null || seats !in 1..8 -> "Le nombre de places doit être compris entre 1 et 8"
            maxPriceText.isNotBlank() && (maxPrice == null || maxPrice <= 0) -> "Le prix maximum doit être positif"
            _date.value?.isBefore(LocalDate.now()) == true -> "La date ne peut pas être passée"
            origin.isNotBlank() && origin.trim().equals(destination.trim(), ignoreCase = true) -> "Le départ et l'arrivée doivent être différents"
            else -> null
        }
        if (error != null) {
            _message.value = Event(error)
            return
        }
        runSearch(RideSearchFilters(origin, destination, _date.value?.toString().orEmpty(), seats!!, maxPrice, sort))
    }

    private fun runSearch(filters: RideSearchFilters) {
        lastFilters = filters
        _results.value = UiState.Loading
        viewModelScope.launch {
            _results.value = when (val result = rideRepository.search(filters)) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
    }

    /** Nombre de places proposé par défaut : celui recherché, borné par les places restantes. */
    fun defaultSeats(ride: RideDto): Int = lastFilters.seats.coerceIn(1, ride.seatsAvailable.coerceAtLeast(1))

    /** Montant total affiché avant confirmation : prix par place × nombre de places. */
    fun totalPrice(ride: RideDto, seats: Int): String = Formatters.price(ride.price * seats)

    fun reserve(ride: RideDto, seats: Int) {
        if (seats !in 1..ride.seatsAvailable) {
            _message.value = Event("Places insuffisantes : ${ride.seatsAvailable} place(s) disponible(s)")
            return
        }
        _reserving.value = true
        viewModelScope.launch {
            when (val result = reservationRepository.reserve(ride.id, seats)) {
                is Resource.Success -> {
                    val confirmed = result.data.status == "confirmed"
                    _message.value = Event(
                        if (confirmed) "Réservation confirmée : ${Formatters.price(result.data.amount)}"
                        else "Demande envoyée au conducteur. Vous serez notifié de sa décision."
                    )
                    runSearch(lastFilters)
                }
                is Resource.Error -> _message.value = Event(result.message)
            }
            _reserving.value = false
        }
    }
}
