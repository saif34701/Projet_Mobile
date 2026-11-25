package com.covoitall.mobile.ui.common

/** État d'une opération affichée à l'écran. */
sealed class UiState<out T> {
    data object Idle : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}

/** Événement à consommer une seule fois (message, navigation), même après une rotation d'écran. */
class Event<out T>(private val content: T) {
    private var handled = false

    fun consume(): T? = if (handled) null else {
        handled = true
        content
    }
}
