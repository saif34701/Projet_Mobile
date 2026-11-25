package com.covoitall.mobile.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.covoitall.mobile.databinding.ActivityLoginBinding
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showError
import com.covoitall.mobile.ui.common.value

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private val viewModel: AuthViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.loginButton.setOnClickListener { viewModel.login(binding.emailInput.value(), binding.passwordInput.value()) }
        binding.registerLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }

        viewModel.fieldErrors.observe(this) { errors ->
            binding.emailLayout.error = errors[AuthField.EMAIL]
            binding.passwordLayout.error = errors[AuthField.PASSWORD]
        }
        viewModel.authState.observe(this) { state ->
            binding.loading.isVisible = state is UiState.Loading
            binding.loginButton.isEnabled = state !is UiState.Loading
            binding.errorText.showError((state as? UiState.Error)?.message)
            // La session est enregistrée dans Room : MainActivity bascule automatiquement sur le tableau de bord.
            if (state is UiState.Success) finish()
        }
    }
}
