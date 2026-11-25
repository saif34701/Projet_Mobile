package com.covoitall.mobile.ui.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.data.remote.ReservationDto
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.data.repository.RideRepository
import com.covoitall.mobile.ui.common.Event
import com.covoitall.mobile.ui.common.UiState
import kotlinx.coroutines.launch

/** Demandes de réservation reçues sur un trajet : acceptation ou refus par le conducteur. */
class RideReservationsViewModel(private val repository: RideRepository) : ViewModel() {
    private var rideId: String? = null

    private val _ride = MutableLiveData<RideEntity?>()
    val ride: LiveData<RideEntity?> = _ride

    private val _reservations = MutableLiveData<UiState<List<ReservationDto>>>(UiState.Idle)
    val reservations: LiveData<UiState<List<ReservationDto>>> = _reservations

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun load(id: String) {
        if (rideId == id) return
        rideId = id
        refresh()
    }

    fun refresh() {
        val id = rideId ?: return
        _reservations.value = UiState.Loading
        viewModelScope.launch {
            _ride.value = repository.ride(id)
            _reservations.value = when (val result = repository.reservations(id)) {
                is Resource.Success -> UiState.Success(result.data.sortedBy { order(it.status) })
                is Resource.Error -> UiState.Error(result.message)
            }
        }
    }

    /** Vérifie localement qu'il reste assez de places avant d'accepter (le serveur revérifie). */
    fun canAccept(reservation: ReservationDto): Boolean {
        val ride = _ride.value ?: return true
        return reservation.status == "pending" && reservation.seats <= ride.seatsAvailable
    }

    fun accept(reservation: ReservationDto) = decide(reservation, true)

    fun refuse(reservation: ReservationDto) = decide(reservation, false)

    private fun decide(reservation: ReservationDto, accept: Boolean) {
        if (accept && !canAccept(reservation)) {
            _message.value = Event("Places insuffisantes pour accepter cette demande")
            return
        }
        viewModelScope.launch {
            when (val result = repository.decide(reservation.id, accept)) {
                is Resource.Success -> {
                    _message.value = Event(if (accept) "Réservation de ${reservation.passengerName} acceptée" else "Demande refusée")
                    refresh()
                }
                is Resource.Error -> _message.value = Event(result.message)
            }
        }
    }

    private fun order(status: String) = when (status) {
        "pending" -> 0
        "confirmed", "paid" -> 1
        else -> 2
    }
}
