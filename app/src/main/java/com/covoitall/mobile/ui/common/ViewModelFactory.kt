package com.covoitall.mobile.ui.common

import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.covoitall.mobile.AppContainer
import com.covoitall.mobile.CovoitallApp
import com.covoitall.mobile.ui.auth.AuthViewModel
import com.covoitall.mobile.ui.driver.DriverRidesViewModel
import com.covoitall.mobile.ui.driver.RideFormViewModel
import com.covoitall.mobile.ui.driver.RideReservationsViewModel
import com.covoitall.mobile.ui.lines.LineDetailViewModel
import com.covoitall.mobile.ui.lines.LinesViewModel
import com.covoitall.mobile.ui.profile.ProfileViewModel
import com.covoitall.mobile.ui.rides.MyReservationsViewModel
import com.covoitall.mobile.ui.rides.RideSearchViewModel

/**
 * Fabrique unique des ViewModels : chaque ViewModel reçoit par constructeur
 * le ou les repositories dont il a besoin (AuthRepository séparé des repositories métier).
 */
class ViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = with(container) {
        when (modelClass) {
            AuthViewModel::class.java -> AuthViewModel(authRepository)
            ProfileViewModel::class.java -> ProfileViewModel(authRepository)
            LinesViewModel::class.java -> LinesViewModel(lineRepository)
            LineDetailViewModel::class.java -> LineDetailViewModel(lineRepository)
            RideSearchViewModel::class.java -> RideSearchViewModel(rideRepository, reservationRepository)
            MyReservationsViewModel::class.java -> MyReservationsViewModel(reservationRepository)
            DriverRidesViewModel::class.java -> DriverRidesViewModel(rideRepository, authRepository)
            RideFormViewModel::class.java -> RideFormViewModel(rideRepository)
            RideReservationsViewModel::class.java -> RideReservationsViewModel(rideRepository)
            else -> throw IllegalArgumentException("ViewModel inconnu : ${modelClass.name}")
        } as T
    }
}

/** Raccourci pour les Activities : `private val viewModel: XViewModel by appViewModels()`. */
inline fun <reified VM : ViewModel> ComponentActivity.appViewModels(): Lazy<VM> =
    viewModels { ViewModelFactory((application as CovoitallApp).container) }
