package com.covoitall.mobile.data.repository

import com.covoitall.mobile.data.local.ReservationDao
import com.covoitall.mobile.data.local.ReservationEntity
import com.covoitall.mobile.data.remote.ApiService
import com.covoitall.mobile.data.remote.ReservationRequest
import com.covoitall.mobile.data.remote.ReservationUpdateRequest
import kotlinx.coroutines.flow.Flow

/** Réservations du passager : création (POST), consultation (GET), modification/annulation (PUT) et suppression (DELETE). */
class ReservationRepository(private val api: ApiService, private val dao: ReservationDao) {
    val myReservations: Flow<List<ReservationEntity>> = dao.observeAll()

    suspend fun refresh(): Resource<Unit> = safeApiCall { api.myReservations() }.map { list ->
        dao.replaceAll(list.map { it.toEntity() })
    }

    suspend fun reserve(rideId: String, seats: Int): Resource<ReservationEntity> =
        safeApiCall { api.reserve(ReservationRequest(rideId, seats)) }.map { it.toEntity().also { entity -> dao.upsert(entity) } }

    suspend fun changeSeats(reservationId: String, seats: Int): Resource<ReservationEntity> =
        safeApiCall { api.updateReservation(reservationId, ReservationUpdateRequest(seats = seats)) }
            .map { it.toEntity().also { entity -> dao.upsert(entity) } }

    suspend fun cancel(reservationId: String): Resource<ReservationEntity> =
        safeApiCall { api.updateReservation(reservationId, ReservationUpdateRequest(status = "cancelled")) }
            .map { it.toEntity().also { entity -> dao.upsert(entity) } }

    suspend fun delete(reservationId: String): Resource<Unit> =
        safeApiCall { api.deleteReservation(reservationId) }.map { dao.delete(reservationId) }
}
