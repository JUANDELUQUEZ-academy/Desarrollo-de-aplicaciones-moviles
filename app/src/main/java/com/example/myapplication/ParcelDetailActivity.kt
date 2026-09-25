package com.example.myapplication

import android.os.Bundle
import com.example.myapplication.data.AppException
import com.example.myapplication.data.local.Roles
import com.example.myapplication.data.local.User
import com.example.myapplication.databinding.ActivityParcelDetailBinding
import com.example.myapplication.ui.ProtectedActivity
import com.example.myapplication.ui.formatTimestamp

class ParcelDetailActivity : ProtectedActivity() {
    override val requiredRole = Roles.CLIENT
    private lateinit var binding: ActivityParcelDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParcelDetailBinding.inflate(layoutInflater)
        bindScreen(binding.root, binding.tvStatus, binding.progress)
        binding.btnBack.setOnClickListener { finish() }
    }

    override suspend fun onAuthenticated(user: User) {
        listOf(binding.tvCode, binding.tvDescription, binding.tvRecipient, binding.tvSummary,
            binding.tvNotes, binding.tvDates).forEach { it.text = "" }
        val parcel = parcels.detailForClient(user.id, intent.getLongExtra("parcelId", 0L))
            ?: throw AppException(getString(R.string.package_missing))
        binding.tvCode.text = parcel.codigo
        binding.tvDescription.text = parcel.descripcion
        binding.tvRecipient.text = getString(R.string.recipient_summary, getString(R.string.person_name, user.nombre, user.apellidos))
        binding.tvSummary.text = getString(R.string.delivery_summary, parcel.estado, parcel.origen, parcel.destino,
            parcel.fechaEstimada.ifEmpty { getString(R.string.no_estimated_date) })
        binding.tvNotes.text = parcel.notas.ifEmpty { getString(R.string.no_notes) }
        binding.tvDates.text = getString(R.string.parcel_dates,
            formatTimestamp(parcel.creadoEn), formatTimestamp(parcel.actualizadoEn))
    }
}
