package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.data.local.Roles
import com.example.myapplication.data.local.User
import com.example.myapplication.databinding.ActivityTrackingBinding
import com.example.myapplication.ui.ProtectedActivity

class TrackingActivity : ProtectedActivity() {
    override val requiredRole = Roles.CLIENT
    private lateinit var binding: ActivityTrackingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTrackingBinding.inflate(layoutInflater)
        bindScreen(binding.root, binding.tvStatus, binding.progress)
        binding.etTrackingCode.doAfterTextChanged {
            binding.trackingInput.error = null
            binding.tvStatus.isVisible = false
        }
        binding.btnSearch.setOnClickListener {
            val code = binding.etTrackingCode.text.toString().trim()
            binding.trackingInput.error = if (code.isEmpty()) getString(R.string.required_field) else null
            if (code.isNotEmpty()) runAction {
                val parcel = parcels.findForClient(actorId, code)
                if (parcel == null) showMessage(getString(R.string.package_missing))
                else startActivity(Intent(this, ParcelDetailActivity::class.java).putExtra("parcelId", parcel.id))
            }
        }
        binding.btnProfile.setOnClickListener { startActivity(Intent(this, ProfileActivity::class.java)) }
        binding.btnLogout.setOnClickListener { logout() }
    }

    override suspend fun onAuthenticated(user: User) {
        binding.tvWelcome.text = getString(R.string.client_welcome, user.nombre)
    }
}
