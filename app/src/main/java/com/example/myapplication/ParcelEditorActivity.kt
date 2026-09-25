package com.example.myapplication

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.data.ParcelForm
import com.example.myapplication.data.ParcelValidator
import com.example.myapplication.data.local.ClientSummary
import com.example.myapplication.data.local.ParcelStatus
import com.example.myapplication.data.local.Roles
import com.example.myapplication.data.local.User
import com.example.myapplication.databinding.ActivityParcelEditorBinding
import com.example.myapplication.ui.ProtectedActivity
import com.example.myapplication.ui.formatTimestamp
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ParcelEditorActivity : ProtectedActivity() {
    override val requiredRole = Roles.ADMIN
    private lateinit var binding: ActivityParcelEditorBinding
    private val parcelId get() = intent.getLongExtra("parcelId", 0L)
    private var clients = emptyList<ClientSummary>()
    private var initialized = false
    private var selectedClientId = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityParcelEditorBinding.inflate(layoutInflater)
        bindScreen(binding.root, binding.tvStatus, binding.progress)
        initialized = savedInstanceState?.getBoolean("initialized") ?: false
        selectedClientId = savedInstanceState?.getLong("clientId") ?: 0L
        binding.tvTitle.setText(if (parcelId == 0L) R.string.package_new_title else R.string.package_edit_title)
        binding.spStatus.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ParcelStatus.values)
        binding.btnDelete.isVisible = parcelId != 0L
        binding.btnCancel.setOnClickListener { finish() }
        listOf(binding.codeInput, binding.descriptionInput, binding.originInput,
            binding.destinationInput, binding.estimatedInput, binding.notesInput).forEach { field ->
            field.editText?.doAfterTextChanged { field.error = null; binding.tvStatus.isVisible = false }
        }
        binding.btnSave.setOnClickListener { save() }
        binding.btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_title)
                .setMessage(getString(R.string.delete_message, binding.etCode.text.toString()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete_confirm) { _, _ ->
                    runAction {
                        parcels.delete(actorId, parcelId)
                        Toast.makeText(this, R.string.package_deleted, Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }.show()
        }
    }

    override suspend fun onAuthenticated(user: User) {
        val parcel = if (parcelId == 0L) null else parcels.forAdmin(user.id, parcelId)
        clients = parcels.clients(user.id)
        if (!initialized && parcel != null) {
            binding.etCode.setText(parcel.codigo)
            binding.etDescription.setText(parcel.descripcion)
            binding.etOrigin.setText(parcel.origen)
            binding.etDestination.setText(parcel.destino)
            binding.etEstimated.setText(parcel.fechaEstimada)
            binding.etNotes.setText(parcel.notas)
            binding.spStatus.setSelection(ParcelStatus.values.indexOf(parcel.estado).coerceAtLeast(0))
            selectedClientId = parcel.clienteId
        }
        binding.spClient.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf(getString(R.string.select_client)) + clients.map { getString(R.string.client_selection, it.nombre, it.apellidos, it.usuario) })
        binding.spClient.setSelection(clients.indexOfFirst { it.id == selectedClientId } + 1)
        binding.tvDates.isVisible = parcel != null
        if (parcel != null) binding.tvDates.text = getString(R.string.parcel_dates,
            formatTimestamp(parcel.creadoEn), formatTimestamp(parcel.actualizadoEn))
        if (clients.isEmpty()) showMessage(getString(R.string.empty_clients))
        initialized = true
    }

    private fun save() {
        val data = ParcelForm(
            binding.etCode.text.toString(), clients.getOrNull(binding.spClient.selectedItemPosition - 1)?.id ?: 0L,
            binding.etDescription.text.toString(), binding.etOrigin.text.toString(),
            binding.etDestination.text.toString(), ParcelStatus.values[binding.spStatus.selectedItemPosition],
            binding.etEstimated.text.toString(), binding.etNotes.text.toString()
        )
        val errors = ParcelValidator.validate(data)
        val fields = mapOf("codigo" to binding.codeInput, "descripcion" to binding.descriptionInput,
            "origen" to binding.originInput, "destino" to binding.destinationInput,
            "fecha" to binding.estimatedInput, "notas" to binding.notesInput)
        fields.forEach { (key, view) -> view.error = errors[key] }
        if (errors.isNotEmpty()) {
            showMessage(errors.values.first())
            fields[errors.keys.first()]?.editText?.requestFocus()
            return
        }
        runAction {
            parcels.save(actorId, parcelId, data)
            Toast.makeText(this, R.string.package_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("initialized", initialized)
        outState.putLong("clientId", clients.getOrNull(binding.spClient.selectedItemPosition - 1)?.id ?: selectedClientId)
        super.onSaveInstanceState(outState)
    }

    override fun onStop() {
        // Al volver desde otra aplicación se recarga el selector sin perder el destinatario elegido.
        selectedClientId = clients.getOrNull(binding.spClient.selectedItemPosition - 1)?.id ?: 0L
        super.onStop()
    }
}
