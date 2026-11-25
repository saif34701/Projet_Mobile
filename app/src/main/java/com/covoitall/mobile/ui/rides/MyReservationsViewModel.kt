package com.covoitall.mobile.ui.rides

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.ReservationEntity
import com.covoitall.mobile.data.repository.ReservationRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.ui.common.Event
import com.covoitall.mobile.ui.common.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class ReservationSummary(val upcoming: Int, val toPay: String, val spent: String)

class MyReservationsViewModel(private val repository: ReservationRepository) : ViewModel() {
    private val showHistory = MutableStateFlow(false)

    val reservations: LiveData<List<ReservationEntity>> = combine(repository.myReservations, showHistory) { list, history ->
        list.filter { isActive(it) != history }
    }.asLiveData()

    /** Indicateurs calculés : réservations à venir, montant restant à payer, total dépensé. */
    val summary: LiveData<ReservationSummary> = repository.myReservations.map { list ->
        ReservationSummary(
            upcoming = list.count { isActive(it) },
            toPay = Formatters.price(list.filter { it.status == "confirmed" && it.rideStatus == "scheduled" }.sumOf { it.amount }),
            spent = Formatters.price(list.filter { it.status == "paid" }.sumOf { it.amount })
        )
    }.asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    init {
        refresh()
    }

    fun showHistory(value: Boolean) {
        showHistory.value = value
    }

    /** Une réservation peut être modifiée tant qu'elle est ouverte et que le trajet n'a pas commencé. */
    fun canModify(item: ReservationEntity) = item.status in listOf("pending", "confirmed") && item.rideStatus == "scheduled"

    fun canDelete(item: ReservationEntity) = item.status in listOf("rejected", "cancelled") || (item.status == "pending" && item.rideStatus != "scheduled")

    /** Places sélectionnables : celles restantes sur le trajet + celles déjà détenues si la réservation est confirmée. */
    fun maxSeats(item: ReservationEntity) =
        (item.rideSeatsAvailable + if (item.status == "confirmed") item.seats else 0).coerceIn(item.seats, 8)

    fun refresh() {
        _loading.value = true
        viewModelScope.launch {
            val result = repository.refresh()
            _loading.value = false
            if (result is Resource.Error) _message.value = Event(result.message)
        }
    }

    fun cancel(item: ReservationEntity) = execute("Réservation annulée") { repository.cancel(item.id) }

    fun delete(item: ReservationEntity) = execute("Réservation retirée") { repository.delete(item.id) }

    fun changeSeats(item: ReservationEntity, seats: Int) {
        if (seats == item.seats) return
        execute("Réservation mise à jour : $seats place(s) · ${Formatters.price(seats * item.pricePerSeat)}") { repository.changeSeats(item.id, seats) }
    }

    private fun execute(success: String, action: suspend () -> Resource<*>) {
        _loading.value = true
        viewModelScope.launch {
            val result = action()
            _loading.value = false
            _message.value = Event(if (result is Resource.Error) result.message else success)
            if (result is Resource.Error) repository.refresh()
        }
    }

    private fun isActive(item: ReservationEntity) =
        item.status in listOf("pending", "confirmed", "paid") && item.rideStatus in listOf("scheduled", "arrived")
}
