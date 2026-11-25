package com.covoitall.mobile.data.repository

import com.covoitall.mobile.data.local.LineEntity
import com.covoitall.mobile.data.local.ReservationEntity
import com.covoitall.mobile.data.local.RideEntity
import com.covoitall.mobile.data.local.ScheduleEntity
import com.covoitall.mobile.data.local.StopEntity
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.data.remote.LineDto
import com.covoitall.mobile.data.remote.ReservationDto
import com.covoitall.mobile.data.remote.RideDto
import com.covoitall.mobile.data.remote.ScheduleDto
import com.covoitall.mobile.data.remote.StopDto
import com.covoitall.mobile.data.remote.UserDto

fun UserDto.toEntity(token: String) = UserEntity(id, firstName, lastName, email, phone, role, driverStatus, token)

fun LineDto.toEntity() = LineEntity(id, name, code, stopIds)

fun StopDto.toEntity() = StopEntity(id, name, address, latitude, longitude)

fun ScheduleDto.toEntity() = ScheduleEntity(id, lineId, departureTime, arrivalTime, days)

fun RideDto.toEntity() = RideEntity(
    id = id,
    origin = origin,
    destination = destination,
    departureAt = departureAt,
    seatsTotal = seatsTotal,
    seatsReserved = seatsReserved,
    seatsAvailable = seatsAvailable,
    pendingRequests = pendingRequests,
    price = price,
    rideType = rideType,
    recurrenceDays = recurrenceDays,
    acceptanceMode = acceptanceMode,
    status = status
)

fun ReservationDto.toEntity() = ReservationEntity(
    id = id,
    rideId = rideId,
    seats = seats,
    amount = amount,
    status = status,
    paymentStatus = paymentStatus,
    createdAt = createdAt,
    origin = ride?.origin ?: "Trajet supprimé",
    destination = ride?.destination ?: "",
    departureAt = ride?.departureAt ?: createdAt,
    driverName = ride?.driverName ?: "",
    rideStatus = ride?.status ?: "cancelled",
    pricePerSeat = ride?.price ?: 0.0,
    rideSeatsAvailable = ride?.seatsAvailable ?: 0
)
