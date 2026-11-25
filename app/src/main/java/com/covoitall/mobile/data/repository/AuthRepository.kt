package com.covoitall.mobile.data.repository

import com.covoitall.mobile.data.local.ReservationDao
import com.covoitall.mobile.data.local.RideDao
import com.covoitall.mobile.data.local.SessionDao
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.data.remote.ApiService
import com.covoitall.mobile.data.remote.AuthResponse
import com.covoitall.mobile.data.remote.LoginRequest
import com.covoitall.mobile.data.remote.ProfileUpdateRequest
import com.covoitall.mobile.data.remote.RegisterRequest
import kotlinx.coroutines.flow.Flow

/**
 * Authentification et session : seul responsable du jeton.
 * La session (utilisateur + jeton JWT) est persistée dans la table Room « session_user ».
 */
class AuthRepository(
    private val api: ApiService,
    private val sessionDao: SessionDao,
    private val rideDao: RideDao,
    private val reservationDao: ReservationDao
) {
    /** Utilisateur authentifié, ou null : source unique de vérité pour l'état de connexion. */
    val authenticatedUser: Flow<UserEntity?> = sessionDao.observe()

    suspend fun login(email: String, password: String): Resource<UserEntity> =
        openSession(safeApiCall { api.login(LoginRequest(email, password)) })

    suspend fun register(request: RegisterRequest): Resource<UserEntity> =
        openSession(safeApiCall { api.register(request) })

    private suspend fun openSession(result: Resource<AuthResponse>): Resource<UserEntity> = when (result) {
        is Resource.Success -> {
            // Nouvelle session : on purge le cache de l'utilisateur précédent.
            rideDao.clear()
            reservationDao.clear()
            val user = result.data.user.toEntity(result.data.accessToken)
            sessionDao.replace(user)
            Resource.Success(user)
        }
        is Resource.Error -> result
    }

    /** Resynchronise le profil (ex. dossier conducteur validé depuis la dernière connexion). */
    suspend fun refreshProfile(): Resource<UserEntity> {
        val session = sessionDao.current() ?: return Resource.Error("Aucune session ouverte")
        return when (val result = safeApiCall { api.profile() }) {
            is Resource.Success -> {
                val user = result.data.toEntity(session.token)
                sessionDao.insert(user)
                Resource.Success(user)
            }
            is Resource.Error -> result
        }
    }

    suspend fun updateProfile(request: ProfileUpdateRequest): Resource<UserEntity> {
        val session = sessionDao.current() ?: return Resource.Error("Aucune session ouverte")
        return safeApiCall { api.updateProfile(request) }.map { dto ->
            dto.toEntity(session.token).also { sessionDao.insert(it) }
        }
    }

    suspend fun logout() {
        sessionDao.clear()
        rideDao.clear()
        reservationDao.clear()
    }
}
