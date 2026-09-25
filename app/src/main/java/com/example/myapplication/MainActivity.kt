package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.example.myapplication.data.RegistrationValidator
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.Roles
import com.example.myapplication.databinding.ActivityMainBinding
import com.example.myapplication.ui.applyScreenInsets
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var busy = false
    private val register = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            binding.etUsername.setText(result.data?.getStringExtra("usuario").orEmpty())
            binding.etPassword.text?.clear()
            binding.tvStatus.text = getString(if (result.data?.getBooleanExtra("adminCreated", false) == true)
                R.string.admin_created else R.string.registration_success)
            binding.tvStatus.isVisible = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        applyScreenInsets(binding.root)
        listOf(binding.usernameInput, binding.passwordInput).forEach { field ->
            field.editText?.doAfterTextChanged { field.error = null; binding.tvStatus.isVisible = false }
        }
        binding.btnCreateAccount.setOnClickListener {
            register.launch(Intent(this, RegisterActivity::class.java))
        }
        binding.btnLogin.setOnClickListener { login() }
        binding.btnSetupAdmin.setOnClickListener {
            register.launch(Intent(this, RegisterActivity::class.java).putExtra("setupAdmin", true))
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            try {
                binding.btnSetupAdmin.isVisible = !AppDatabase.getInstance(applicationContext).userDao().hayAdministrador()
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (_: Exception) { binding.btnSetupAdmin.isVisible = false }
        }
    }

    private fun login() {
        if (busy) return
        val username = RegistrationValidator.normalizeUser(binding.etUsername.text.toString())
        val password = binding.etPassword.text.toString()
        binding.usernameInput.error = if (username.isBlank()) getString(R.string.required_field) else null
        binding.passwordInput.error = if (password.isEmpty()) getString(R.string.required_field) else null
        binding.tvStatus.isVisible = false
        if (username.isBlank() || password.isEmpty()) return
        setBusy(true)
        lifecycleScope.launch {
            try {
                val user = UserRepository(AppDatabase.getInstance(applicationContext).userDao()).login(username, password)
                if (user == null) {
                    binding.tvStatus.text = getString(R.string.invalid_credentials)
                    binding.tvStatus.isVisible = true
                } else {
                    Session.start(user.id)
                    binding.etPassword.text?.clear()
                    startActivity(Intent(this@MainActivity,
                        if (user.rol == Roles.ADMIN) AdminActivity::class.java else TrackingActivity::class.java))
                    finish()
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                binding.tvStatus.text = getString(R.string.database_error)
                binding.tvStatus.isVisible = true
            } finally { setBusy(false) }
        }
    }

    private fun setBusy(value: Boolean) {
        busy = value
        binding.progress.isVisible = value
        binding.btnLogin.isEnabled = !value
        binding.btnCreateAccount.isEnabled = !value
        binding.btnSetupAdmin.isEnabled = !value
        binding.etUsername.isEnabled = !value
        binding.etPassword.isEnabled = !value
    }
}
