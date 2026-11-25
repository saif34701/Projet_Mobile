package com.covoitall.mobile.data.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.QueryMap

/** Contrat Retrofit de l'API FastAPI COVOIT'ALL : chaque opération est une fonction suspendue. */
interface ApiService {

    // --- Authentification et profil (AuthRepository) ---
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/signup")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("profile")
    suspend fun profile(): Response<UserDto>

    @PUT("profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): Response<UserDto>

    // --- Lignes de navette (LineRepository) ---
    @GET("network/lines")
    suspend fun lines(): Response<List<LineDto>>

    @GET("network/stops")
    suspend fun stops(): Response<List<StopDto>>

    @GET("network/schedules")
    suspend fun schedules(): Response<List<ScheduleDto>>

    @GET("network/lines/{lineId}/live")
    suspend fun liveLine(@Path("lineId") lineId: String): Response<LiveLineDto>

    // --- Trajets publiés par le conducteur (RideRepository) ---
    @GET("rides/search")
    suspend fun searchRides(@QueryMap filters: Map<String, String>): Response<List<RideDto>>

    @GET("rides/mine")
    suspend fun myRides(): Response<List<RideDto>>

    @POST("rides")
    suspend fun publishRide(@Body request: RideRequest): Response<RideDto>

    @PUT("rides/{rideId}")
    suspend fun updateRide(@Path("rideId") rideId: String, @Body request: RideRequest): Response<RideDto>

    @DELETE("rides/{rideId}")
    suspend fun deleteRide(@Path("rideId") rideId: String): Response<Unit>

    @PUT("rides/{rideId}/status")
    suspend fun updateRideStatus(@Path("rideId") rideId: String, @Body request: RideStatusRequest): Response<RideDto>

    @GET("rides/{rideId}/reservations")
    suspend fun rideReservations(@Path("rideId") rideId: String): Response<List<ReservationDto>>

    // --- Réservations du passager (ReservationRepository) ---
    @GET("reservations/mine")
    suspend fun myReservations(): Response<List<ReservationDto>>

    @POST("reservations")
    suspend fun reserve(@Body request: ReservationRequest): Response<ReservationDto>

    @PUT("reservations/{reservationId}")
    suspend fun updateReservation(@Path("reservationId") reservationId: String, @Body request: ReservationUpdateRequest): Response<ReservationDto>

    @DELETE("reservations/{reservationId}")
    suspend fun deleteReservation(@Path("reservationId") reservationId: String): Response<Unit>
}
