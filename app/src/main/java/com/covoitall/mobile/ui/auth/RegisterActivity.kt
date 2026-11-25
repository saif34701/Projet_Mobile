package com.covoitall.mobile.ui.auth

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.covoitall.mobile.R
import com.covoitall.mobile.databinding.ActivityRegisterBinding
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showError
import com.covoitall.mobile.ui.common.value

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by appViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.roleToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) binding.driverHint.isVisible = checkedId == R.id.driverRole
        }
        binding.registerButton.setOnClickListener {
            viewModel.register(
                firstName = binding.firstNameInput.value(),
                lastName = binding.lastNameInput.value(),
                email = binding.emailInput.value(),
                phone = binding.phoneInput.value(),
                password = binding.passwordInput.value(),
                confirm = binding.confirmInput.value(),
                asDriver = binding.roleToggle.checkedButtonId == R.id.driverRole
            )
        }

        viewModel.fieldErrors.observe(this) { errors ->
            binding.firstNameLayout.error = errors[AuthField.FIRST_NAME]
            binding.lastNameLayout.error = errors[AuthField.LAST_NAME]
            binding.emailLayout.error = errors[AuthField.EMAIL]
            binding.phoneLayout.error = errors[AuthField.PHONE]
            binding.passwordLayout.error = errors[AuthField.PASSWORD]
            binding.confirmLayout.error = errors[AuthField.CONFIRM]
        }
        viewModel.authState.observe(this) { state ->
            binding.loading.isVisible = state is UiState.Loading
            binding.registerButton.isEnabled = state !is UiState.Loading
            binding.errorText.showError((state as? UiState.Error)?.message)
            if (state is UiState.Success) finish()
        }
    }
}
