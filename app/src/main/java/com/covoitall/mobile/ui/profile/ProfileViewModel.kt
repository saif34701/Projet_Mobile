package com.covoitall.mobile.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.data.remote.ProfileUpdateRequest
import com.covoitall.mobile.data.repository.AuthRepository
import com.covoitall.mobile.data.repository.Resource
import com.covoitall.mobile.ui.common.Event
import com.covoitall.mobile.ui.common.UiState
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: AuthRepository) : ViewModel() {
    val user: LiveData<UserEntity?> = repository.authenticatedUser.asLiveData()

    private val _saveState = MutableLiveData<UiState<Unit>>(UiState.Idle)
    val saveState: LiveData<UiState<Unit>> = _saveState

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> = _message

    fun save(firstName: String, lastName: String, phone: String) {
        val error = when {
            firstName.trim().length < 2 -> "Le prénom doit contenir au moins 2 caractères"
            lastName.trim().length < 2 -> "Le nom doit contenir au moins 2 caractères"
            phone.count { it.isDigit() } < 8 -> "Numéro de téléphone invalide"
            else -> null
        }
        if (error != null) {
            _saveState.value = UiState.Error(error)
            return
        }
        _saveState.value = UiState.Loading
        viewModelScope.launch {
            when (val result = repository.updateProfile(ProfileUpdateRequest(firstName.trim(), lastName.trim(), phone.trim()))) {
                is Resource.Success -> {
                    _saveState.value = UiState.Success(Unit)
                    _message.value = Event("Profil mis à jour")
                }
                is Resource.Error -> _saveState.value = UiState.Error(result.message)
            }
        }
    }
}
