package com.covoitall.mobile.ui.driver

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.data.repository.AuthRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.data.repository.RideRepository
import com.covoitall.mobile.ui.common.Event
import com.covoitall.mobile.ui.common.Formatters
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime

/** Indicateurs du conducteur calculés à partir de ses trajets à venir. */
data class DriverStats(val upcoming: Int, val fillRate: Int, val bookedSeats: Int, val offeredSeats: Int, val expectedRevenue: String, val pendingRequests: Int)

class DriverRidesViewModel(
    private val rideRepository: RideRepository,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val showHistory = MutableStateFlow(false)

    val user: LiveData<UserEntity?> = authRepository.authenticatedUser.asLiveData()

    val rides: LiveData<List<RideEntity>> = combine(rideRepository.myRides, showHistory) { rides, history ->
        val filtered = rides.filter { isUpcoming(it) != history }
        if (history) filtered else filtered.sortedBy { it.departureAt }
    }.asLiveData()

    val stats: LiveData<DriverStats> = rideRepository.myRides.map { rides ->
        val upcoming = rides.filter { isUpcoming(it) }
        val offered = upcoming.sumOf { it.seatsTotal }
        val booked = upcoming.sumOf { it.seatsReserved }
        DriverStats(
            upcoming = upcoming.size,
            fillRate = if (offered == 0) 0 else booked * 100 / offered,
            bookedSeats = booked,
            offeredSeats = offered,
            expectedRevenue = Formatters.price(upcoming.sumOf { it.seatsReserved * it.price }),
            pendingRequests = upcoming.sumOf { it.pendingRequests }
        )
    }.asLiveData()

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun showHistory(value: Boolean) {
        showHistory.value = value
    }

    /** Recharge le profil (validation des documents) puis les trajets publiés. */
    fun refresh() {
        _loading.value = true
        viewModelScope.launch {
            authRepository.refreshProfile()
            val result = rideRepository.refreshMyRides()
            _loading.value = false
            if (result is Resource.Error) _message.value = Event(result.message)
        }
    }

    fun canEdit(ride: RideEntity) = ride.status == "scheduled"

    /** Le bouton « Arrivé » n'est proposé que dans les 3 heures précédant le départ et s'il y a des passagers confirmés. */
    fun canMarkArrived(ride: RideEntity): Boolean {
        val departure = Formatters.parse(ride.departureAt) ?: return false
        return ride.status == "scheduled" && ride.seatsReserved > 0 && Duration.between(LocalDateTime.now(), departure).toHours() < 3
    }

    fun canComplete(ride: RideEntity) = ride.status == "arrived"

    fun deleteWarning(ride: RideEntity): String =
        if (ride.seatsReserved + ride.pendingRequests > 0) "Des passagers ont réservé ce trajet : il sera annulé et ils seront prévenus. Continuer ?"
        else "Supprimer définitivement ce trajet ?"

    fun delete(ride: RideEntity) = execute("Trajet supprimé") { rideRepository.delete(ride.id) }

    fun markArrived(ride: RideEntity) = execute("Passagers prévenus de votre arrivée") { rideRepository.updateStatus(ride.id, "arrived") }

    fun markCompleted(ride: RideEntity) = execute("Trajet terminé") { rideRepository.updateStatus(ride.id, "completed") }

    private fun execute(success: String, action: suspend () -> Resource<*>) {
        _loading.value = true
        viewModelScope.launch {
            val result = action()
            _loading.value = false
            _message.value = Event(if (result is Resource.Error) result.message else success)
        }
    }

    private fun isUpcoming(ride: RideEntity) = ride.status == "scheduled" || ride.status == "arrived"
}
