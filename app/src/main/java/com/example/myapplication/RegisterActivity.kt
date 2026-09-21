package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.DuplicateUserException
import com.example.myapplication.data.Registration
import com.example.myapplication.data.RegistrationValidator
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.databinding.ActivityRegisterBinding
import com.example.myapplication.ui.applyScreenInsets
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private var busy = false
    private val fields: Map<String, TextInputLayout>
        get() = mapOf(
            "usuario" to binding.usernameInput, "nombre" to binding.nameInput,
            "apellidos" to binding.surnameInput, "edad" to binding.ageInput,
            "direccion" to binding.addressInput, "telefono" to binding.phoneInput,
            "password" to binding.passwordInput, "confirmation" to binding.confirmInput
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyScreenInsets(binding.root)
        binding.btnCancel.setOnClickListener { if (!busy) finish() }
        binding.btnSubmit.setOnClickListener { submit() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() { if (!busy) finish() }
        })
    }

    private fun submit() {
        if (busy) return
        val data = Registration(
            binding.etUsername.text.toString(), binding.etName.text.toString(),
            binding.etSurname.text.toString(), binding.etAge.text.toString(),
            binding.etAddress.text.toString(), binding.etPhone.text.toString(),
            binding.etPassword.text.toString(), binding.etConfirm.text.toString()
        )
        val errors = RegistrationValidator.validate(data)
        fields.forEach { (name, input) -> input.error = errors[name] }
        binding.tvStatus.isVisible = false
        if (errors.isNotEmpty()) {
            fields[errors.keys.first()]?.editText?.requestFocus()
            return
        }
        setBusy(true)
        lifecycleScope.launch {
            try {
                UserRepository(AppDatabase.getInstance(applicationContext).userDao()).register(data)
                setResult(RESULT_OK, Intent().putExtra("usuario", RegistrationValidator.normalizeUser(data.usuario)))
                binding.etPassword.text?.clear()
                binding.etConfirm.text?.clear()
                finish()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: DuplicateUserException) {
                binding.usernameInput.error = getString(R.string.duplicate_user)
                binding.etUsername.requestFocus()
            } catch (_: Exception) {
                binding.tvStatus.text = getString(R.string.database_error)
                binding.tvStatus.isVisible = true
            } finally { setBusy(false) }
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        binding.progress.isVisible = value
        binding.btnSubmit.isEnabled = !value
        binding.btnCancel.isEnabled = !value
        fields.values.forEach { it.editText?.isEnabled = !value }
    }
}
