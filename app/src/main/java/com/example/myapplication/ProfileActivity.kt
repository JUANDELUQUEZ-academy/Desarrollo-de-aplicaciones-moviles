package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.data.Registration
import com.example.myapplication.data.RegistrationValidator
import com.example.myapplication.data.local.Roles
import com.example.myapplication.data.local.User
import com.example.myapplication.databinding.ActivityProfileBinding
import com.example.myapplication.ui.ProtectedActivity

class ProfileActivity : ProtectedActivity() {
    override val requiredRole = Roles.CLIENT
    private lateinit var binding: ActivityProfileBinding
    private var initialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        bindScreen(binding.root, binding.tvStatus, binding.progress)
        initialized = savedInstanceState?.getBoolean("initialized") ?: false
        binding.btnCancel.setOnClickListener { finish() }
        listOf(binding.usernameInput, binding.nameInput, binding.surnameInput, binding.ageInput,
            binding.addressInput, binding.phoneInput, binding.currentPasswordInput,
            binding.passwordInput, binding.confirmInput).forEach { field ->
            field.editText?.doAfterTextChanged { field.error = null; binding.tvStatus.isVisible = false }
        }
        binding.btnSaveProfile.setOnClickListener { save() }
    }

    override suspend fun onAuthenticated(user: User) {
        if (initialized) return
        binding.etUsername.setText(user.usuario)
        binding.etName.setText(user.nombre)
        binding.etSurname.setText(user.apellidos)
        binding.etAge.setText(String.format(java.util.Locale.ROOT, "%d", user.edad))
        binding.etAddress.setText(user.direccion)
        binding.etPhone.setText(user.telefono)
        initialized = true
    }

    private fun save() {
        val data = Registration(binding.etUsername.text.toString(), binding.etName.text.toString(),
            binding.etSurname.text.toString(), binding.etAge.text.toString(), binding.etAddress.text.toString(),
            binding.etPhone.text.toString(), binding.etPassword.text.toString(), binding.etConfirm.text.toString())
        val errors = RegistrationValidator.validate(data, passwordOptional = true)
        val fields = mapOf("usuario" to binding.usernameInput, "nombre" to binding.nameInput,
            "apellidos" to binding.surnameInput, "edad" to binding.ageInput, "direccion" to binding.addressInput,
            "telefono" to binding.phoneInput, "password" to binding.passwordInput, "confirmation" to binding.confirmInput)
        fields.forEach { (key, view) -> view.error = errors[key] }
        val current = binding.etCurrentPassword.text.toString()
        binding.currentPasswordInput.error = if (current.isEmpty()) getString(R.string.required_field) else null
        if (errors.isNotEmpty() || current.isEmpty()) {
            fields[errors.keys.firstOrNull()]?.editText?.requestFocus()
            return
        }
        runAction {
            users.updateProfile(actorId, data, current)
            binding.etCurrentPassword.text?.clear()
            binding.etPassword.text?.clear()
            binding.etConfirm.text?.clear()
            Toast.makeText(this, R.string.profile_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("initialized", initialized)
        super.onSaveInstanceState(outState)
    }
}
