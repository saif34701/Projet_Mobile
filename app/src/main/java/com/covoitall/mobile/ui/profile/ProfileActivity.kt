package com.covoitall.mobile.ui.profile

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.covoitall.mobile.databinding.ActivityProfileBinding
import com.covoitall.mobile.ui.common.UiState
import com.covoitall.mobile.ui.common.appViewModels
import com.covoitall.mobile.ui.common.showError
import com.covoitall.mobile.ui.common.showMessage
import com.covoitall.mobile.ui.common.value

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val viewModel: ProfileViewModel by appViewModels()
    private var formFilled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        formFilled = savedInstanceState != null

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.saveButton.setOnClickListener {
            viewModel.save(binding.firstNameInput.value(), binding.lastNameInput.value(), binding.phoneInput.value())
        }

        viewModel.user.observe(this) { user ->
            if (user == null) {
                finish()
                return@observe
            }
            binding.nameText.text = user.fullName
            binding.emailText.text = user.email
            binding.roleText.text = if (user.isDriver) "Conducteur" else if (user.isPassenger) "Passager" else user.role
            if (!formFilled) {
                binding.firstNameInput.setText(user.firstName)
                binding.lastNameInput.setText(user.lastName)
                binding.phoneInput.setText(user.phone)
                formFilled = true
            }
        }
        viewModel.saveState.observe(this) { state ->
            binding.saveButton.isEnabled = state !is UiState.Loading
            binding.errorText.showError((state as? UiState.Error)?.message)
        }
        viewModel.message.observe(this) { event -> event.consume()?.let(::showMessage) }
    }
}
