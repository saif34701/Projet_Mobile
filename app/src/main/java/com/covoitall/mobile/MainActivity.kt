package com.covoitall.mobile

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.covoitall.mobile.data.local.UserEntity
import com.covoitall.mobile.databinding.ActivityMainBinding
import com.covoitall.mobile.ui.auth.AuthViewModel
import com.covoitall.mobile.ui.auth.LoginActivity
import com.covoitall.mobile.ui.auth.RegisterActivity
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.driver.DriverRidesActivity
import com.covoitall.mobile.ui.lines.LinesActivity
import com.covoitall.mobile.ui.profile.ProfileActivity
import com.covoitall.mobile.ui.rides.MyReservationsActivity
import com.covoitall.mobile.ui.rides.RideSearchActivity

/**
 * Point d'entrée : l'affichage est entièrement conditionné par
 * authViewModel.authenticatedUser (session persistée dans Room).
 */
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.registerButton.setOnClickListener { open(RegisterActivity::class.java) }
        binding.loginButton.setOnClickListener { open(LoginActivity::class.java) }
        binding.guestLinesButton.setOnClickListener { open(LinesActivity::class.java) }
        binding.searchCard.setOnClickListener { open(RideSearchActivity::class.java) }
        binding.reservationsCard.setOnClickListener { open(MyReservationsActivity::class.java) }
        binding.driverRidesCard.setOnClickListener { open(DriverRidesActivity::class.java) }
        binding.linesCard.setOnClickListener { open(LinesActivity::class.java) }
        binding.profileButton.setOnClickListener { open(ProfileActivity::class.java) }
        binding.logoutButton.setOnClickListener { authViewModel.logout() }

        authViewModel.authenticatedUser.observe(this) { user -> render(user) }
    }

    override fun onStart() {
        super.onStart()
        authViewModel.refreshSession()
    }

    private fun render(user: UserEntity?) {
        binding.startupProgress.visibility = View.GONE
        binding.welcomeContainer.isVisible = user == null
        binding.dashboardContainer.isVisible = user != null
        if (user == null) return

        binding.greetingText.text = "Bonjour ${user.firstName} !"
        binding.roleText.text = when (user.role) {
            UserEntity.ROLE_PASSENGER -> "Passager"
            UserEntity.ROLE_DRIVER -> "Conducteur"
            "shuttle_driver" -> "Chauffeur de navette"
            "admin" -> "Administrateur"
            else -> user.role
        }
        binding.searchCard.isVisible = user.isPassenger
        binding.reservationsCard.isVisible = user.isPassenger
        binding.driverRidesCard.isVisible = user.isDriver
        binding.driverStatusText.isVisible = user.isDriver && !user.isApprovedDriver
        binding.driverStatusText.text = when (user.driverStatus) {
            "pending" -> "Vos documents sont en cours de vérification par l'administrateur. Vous pourrez publier des trajets dès leur validation."
            "rejected" -> "Vos documents ont été refusés. Corrigez votre dossier depuis le site web COVOIT'ALL."
            else -> "Déposez vos documents (permis, assurance, carte grise) depuis le site web COVOIT'ALL pour publier des trajets."
        }
    }

    private fun open(target: Class<*>) = startActivity(Intent(this, target))
}
