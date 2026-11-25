package com.covoitall.mobile.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Utilisateur connecté et son jeton : la présence d'une ligne signifie qu'une session est ouverte. */
@Entity(tableName = "session_user")
data class UserEntity(
    @PrimaryKey val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    val driverStatus: String?,
    val token: String
) {
    val fullName: String get() = "$firstName $lastName"
    val isPassenger: Boolean get() = role == ROLE_PASSENGER
    val isDriver: Boolean get() = role == ROLE_DRIVER
    val isApprovedDriver: Boolean get() = isDriver && driverStatus == "approved"

    companion object {
        const val ROLE_PASSENGER = "passenger"
        const val ROLE_DRIVER = "driver"
    }
}

@Entity(tableName = "lines")
data class LineEntity(@PrimaryKey val id: String, val name: String, val code: String, val stopIds: List<String>)

@Entity(tableName = "stops")
data class StopEntity(@PrimaryKey val id: String, val name: String, val address: String, val latitude: Double, val longitude: Double)

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey val id: String,
    val lineId: String,
    val departureTime: String,
    val arrivalTime: String,
    val days: List<String>
)

/** Trajet publié par le conducteur connecté (cache de « Mes trajets »). */
@Entity(tableName = "my_rides")
data class RideEntity(
    @PrimaryKey val id: String,
    val origin: String,
    val destination: String,
    val departureAt: String,
    val seatsTotal: Int,
    val seatsReserved: Int,
    val seatsAvailable: Int,
    val pendingRequests: Int,
    val price: Double,
    val rideType: String,
    val recurrenceDays: List<String>,
    val acceptanceMode: String,
    val status: String
)

/** Réservation du passager connecté, avec un résumé du trajet pour l'affichage hors ligne. */
@Entity(tableName = "my_reservations")
data class ReservationEntity(
    @PrimaryKey val id: String,
    val rideId: String,
    val seats: Int,
    val amount: Double,
    val status: String,
    val paymentStatus: String,
    val createdAt: String,
    val origin: String,
    val destination: String,
    val departureAt: String,
    val driverName: String,
    val rideStatus: String,
    val pricePerSeat: Double,
    val rideSeatsAvailable: Int
)

class Converters {
    private val gson = Gson()
    private val listType = object : TypeToken<List<String>>() {}.type

    @TypeConverter
    fun fromList(value: List<String>): String = gson.toJson(value)

    @TypeConverter
    fun toList(value: String): List<String> = gson.fromJson(value, listType) ?: emptyList()
}
