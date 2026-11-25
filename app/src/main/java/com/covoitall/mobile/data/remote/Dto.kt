package com.covoitall.mobile.data.remote

import com.google.gson.annotations.SerializedName

// ---------- Authentification ----------

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
    val role: String
)

data class AuthResponse(
    @SerializedName("access_token") val accessToken: String,
    @SerializedName("token_type") val tokenType: String,
    val user: UserDto
)

data class UserDto(
    val id: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val phone: String,
    val role: String,
    @SerializedName("driver_status") val driverStatus: String?
)

data class ProfileUpdateRequest(
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val phone: String
)

// ---------- Réseau de navettes ----------

data class StopDto(val id: String, val name: String, val address: String, val latitude: Double, val longitude: Double)

data class LineDto(val id: String, val name: String, val code: String, @SerializedName("stop_ids") val stopIds: List<String>)

data class ScheduleDto(
    val id: String,
    @SerializedName("line_id") val lineId: String,
    @SerializedName("departure_time") val departureTime: String,
    @SerializedName("arrival_time") val arrivalTime: String,
    val days: List<String>
)

data class LivePositionDto(val latitude: Double, val longitude: Double, @SerializedName("recorded_at") val recordedAt: String)

data class LiveVehicleDto(val model: String, val registration: String, val capacity: Int)

data class LiveStopDto(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerializedName("eta_minutes") val etaMinutes: Int?,
    val passed: Boolean
)

data class NextDepartureDto(val day: String, @SerializedName("departure_time") val departureTime: String, @SerializedName("in_days") val inDays: Int)

data class IncidentDto(val type: String, val description: String, @SerializedName("delay_minutes") val delayMinutes: Int)

data class LiveLineDto(
    @SerializedName("line_id") val lineId: String,
    @SerializedName("line_name") val lineName: String,
    @SerializedName("line_code") val lineCode: String,
    val active: Boolean,
    val position: LivePositionDto?,
    @SerializedName("position_stale") val positionStale: Boolean,
    @SerializedName("delay_minutes") val delayMinutes: Int,
    val vehicle: LiveVehicleDto?,
    @SerializedName("next_stop") val nextStop: String?,
    val stops: List<LiveStopDto>,
    @SerializedName("next_departure") val nextDeparture: NextDepartureDto?,
    val incidents: List<IncidentDto>
)

// ---------- Covoiturage ----------

data class VehicleDto(val model: String?, val registration: String?, val capacity: Int?)

data class RideDto(
    val id: String,
    @SerializedName("driver_id") val driverId: String,
    @SerializedName("driver_name") val driverName: String,
    @SerializedName("driver_rating") val driverRating: Double,
    val vehicle: VehicleDto?,
    val origin: String,
    val destination: String,
    @SerializedName("departure_at") val departureAt: String,
    @SerializedName("seats_total") val seatsTotal: Int,
    @SerializedName("seats_reserved") val seatsReserved: Int,
    @SerializedName("seats_available") val seatsAvailable: Int,
    @SerializedName("pending_requests") val pendingRequests: Int,
    val price: Double,
    @SerializedName("ride_type") val rideType: String,
    @SerializedName("recurrence_days") val recurrenceDays: List<String>,
    @SerializedName("acceptance_mode") val acceptanceMode: String,
    val status: String
)

data class RideRequest(
    val origin: String,
    val destination: String,
    @SerializedName("departure_at") val departureAt: String,
    @SerializedName("seats_total") val seatsTotal: Int,
    val price: Double,
    @SerializedName("ride_type") val rideType: String,
    @SerializedName("recurrence_days") val recurrenceDays: List<String>,
    @SerializedName("acceptance_mode") val acceptanceMode: String
)

data class RideStatusRequest(val status: String)

data class ReservationDto(
    val id: String,
    @SerializedName("ride_id") val rideId: String,
    @SerializedName("passenger_name") val passengerName: String,
    @SerializedName("passenger_phone") val passengerPhone: String,
    val seats: Int,
    val amount: Double,
    val status: String,
    @SerializedName("payment_status") val paymentStatus: String,
    val reviewed: Boolean,
    @SerializedName("created_at") val createdAt: String,
    val ride: RideDto?
)

data class ReservationRequest(@SerializedName("ride_id") val rideId: String, val seats: Int)

/** Les champs nuls ne sont pas sérialisés par Gson : seule la modification demandée est envoyée. */
data class ReservationUpdateRequest(val status: String? = null, val seats: Int? = null)
