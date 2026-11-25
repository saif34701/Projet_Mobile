package com.covoitall.mobile.data.repository

import com.covoitall.mobile.data.local.RideDao
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.data.remote.ApiService
import com.covoitall.mobile.data.remote.ReservationDto
import com.covoitall.mobile.data.remote.ReservationUpdateRequest
import com.covoitall.mobile.data.remote.RideDto
import com.covoitall.mobile.data.remote.RideRequest
import com.covoitall.mobile.data.remote.RideStatusRequest
import kotlinx.coroutines.flow.Flow

/** Critères de recherche d'un trajet (envoyés en paramètres de requête). */
data class RideSearchFilters(
    val origin: String = "",
    val destination: String = "",
    val date: String = "",
    val seats: Int = 1,
    val maxPrice: Double? = null,
    val sort: String = "departure"
) {
    fun toQuery(): Map<String, String> = buildMap {
        if (origin.isNotBlank()) put("origin", origin.trim())
        if (destination.isNotBlank()) put("destination", destination.trim())
        if (date.isNotBlank()) put("date", date)
        put("seats", seats.toString())
        maxPrice?.let { put("max_price", it.toString()) }
        put("sort", sort)
    }
}

/**
 * Trajets de covoiturage : recherche (passager) et gestion des trajets publiés (conducteur).
 * Chaque opération CRUD utilise la méthode HTTP adaptée puis met à jour le cache Room observé par l'UI.
 */
class RideRepository(private val api: ApiService, private val dao: RideDao) {
    val myRides: Flow<List<RideEntity>> = dao.observeAll()

    suspend fun search(filters: RideSearchFilters): Resource<List<RideDto>> = safeApiCall { api.searchRides(filters.toQuery()) }

    suspend fun refreshMyRides(): Resource<Unit> = safeApiCall { api.myRides() }.map { rides ->
        dao.replaceAll(rides.map { it.toEntity() })
    }

    suspend fun ride(rideId: String): RideEntity? = dao.byId(rideId)

    suspend fun publish(request: RideRequest): Resource<RideEntity> =
        safeApiCall { api.publishRide(request) }.map { it.toEntity().also { entity -> dao.upsert(entity) } }

    suspend fun update(rideId: String, request: RideRequest): Resource<RideEntity> =
        safeApiCall { api.updateRide(rideId, request) }.map { it.toEntity().also { entity -> dao.upsert(entity) } }

    /** Suppression (DELETE) : le serveur annule le trajet s'il a déjà des passagers, d'où la resynchronisation. */
    suspend fun delete(rideId: String): Resource<Unit> = when (val result = safeApiCall { api.deleteRide(rideId) }) {
        is Resource.Success -> {
            dao.delete(rideId)
            refreshMyRides()
            Resource.Success(Unit)
        }
        is Resource.Error -> result
    }

    suspend fun updateStatus(rideId: String, status: String): Resource<RideEntity> =
        safeApiCall { api.updateRideStatus(rideId, RideStatusRequest(status)) }.map { it.toEntity().also { entity -> dao.upsert(entity) } }

    suspend fun reservations(rideId: String): Resource<List<ReservationDto>> = safeApiCall { api.rideReservations(rideId) }

    /** Acceptation ou refus d'une demande de réservation par le conducteur. */
    suspend fun decide(reservationId: String, accept: Boolean): Resource<ReservationDto> {
        val result = safeApiCall {
            api.updateReservation(reservationId, ReservationUpdateRequest(status = if (accept) "confirmed" else "rejected"))
        }
        if (result is Resource.Success) refreshMyRides()
        return result
    }
}
