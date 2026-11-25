package com.covoitall.mobile.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.data.remote.RegisterRequest
import com.covoitall.mobile.data.repository.AuthRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.ui.common.UiState
import kotlinx.coroutines.launch

/** Champs des formulaires d'authentification, utilisés pour afficher les erreurs sous chaque saisie. */
enum class AuthField { FIRST_NAME, LAST_NAME, EMAIL, PHONE, PASSWORD, CONFIRM }

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    /** Observé par MainActivity pour afficher l'accueil (déconnecté) ou le tableau de bord (connecté). */
    val authenticatedUser: LiveData<UserEntity?> = repository.authenticatedUser.asLiveData()

    private val _authState = MutableLiveData<UiState<UserEntity>>(UiState.Idle)
    val authState: LiveData<UiState<UserEntity>> = _authState

    private val _fieldErrors = MutableLiveData<Map<AuthField, String>>(emptyMap())
    val fieldErrors: LiveData<Map<AuthField, String>> = _fieldErrors

    fun login(email: String, password: String) {
        val errors = buildMap {
            if (!EMAIL_REGEX.matches(email.trim())) put(AuthField.EMAIL, "Adresse email invalide")
            if (password.isEmpty()) put(AuthField.PASSWORD, "Mot de passe obligatoire")
        }
        if (!validate(errors)) return
        submit { repository.login(email.trim().lowercase(), password) }
    }

    fun register(firstName: String, lastName: String, email: String, phone: String, password: String, confirm: String, asDriver: Boolean) {
        val digits = phone.filter { it.isDigit() }
        val errors = buildMap {
            if (firstName.trim().length < 2) put(AuthField.FIRST_NAME, "2 caractères minimum")
            if (lastName.trim().length < 2) put(AuthField.LAST_NAME, "2 caractères minimum")
            if (!EMAIL_REGEX.matches(email.trim())) put(AuthField.EMAIL, "Adresse email invalide")
            if (digits.length < 8 || phone.trim().length > 20) put(AuthField.PHONE, "Numéro de téléphone invalide (8 chiffres minimum)")
            if (password.length < 8) put(AuthField.PASSWORD, "8 caractères minimum")
            else if (password.none { it.isDigit() } || password.none { it.isLetter() }) put(AuthField.PASSWORD, "Utilisez des lettres et des chiffres")
            if (confirm != password) put(AuthField.CONFIRM, "Les mots de passe ne correspondent pas")
        }
        if (!validate(errors)) return
        val role = if (asDriver) UserEntity.ROLE_DRIVER else UserEntity.ROLE_PASSENGER
        submit { repository.register(RegisterRequest(firstName.trim(), lastName.trim(), email.trim().lowercase(), phone.trim(), password, role)) }
    }

    /** Resynchronise le profil au retour sur l'accueil (statut du dossier conducteur, etc.). */
    fun refreshSession() {
        viewModelScope.launch { repository.refreshProfile() }
    }

    fun logout() {
        viewModelScope.launch { repository.logout() }
    }

    private fun validate(errors: Map<AuthField, String>): Boolean {
        _fieldErrors.value = errors
        return errors.isEmpty()
    }

    private fun submit(action: suspend () -> Resource<UserEntity>) {
        if (_authState.value is UiState.Loading) return
        _authState.value = UiState.Loading
        viewModelScope.launch {
            _authState.value = when (val result = action()) {
                is Resource.Success -> UiState.Success(result.data)
                is Resource.Error -> UiState.Error(result.message)
            }
        }
    }

    companion object {
        private val EMAIL_REGEX = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    }
}
