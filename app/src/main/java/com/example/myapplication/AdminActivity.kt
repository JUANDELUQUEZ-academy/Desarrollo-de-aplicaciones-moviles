package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.example.myapplication.data.local.ClientSummary
import com.example.myapplication.data.local.Parcel
import com.example.myapplication.data.local.Roles
import com.example.myapplication.data.local.User
import com.example.myapplication.databinding.ActivityAdminBinding
import com.example.myapplication.databinding.ItemClientBinding
import com.example.myapplication.databinding.ItemParcelBinding
import com.example.myapplication.ui.ProtectedActivity

class AdminActivity : ProtectedActivity() {
    override val requiredRole = Roles.ADMIN
    private lateinit var binding: ActivityAdminBinding
    private var clients = emptyList<ClientSummary>()
    private var packages = emptyList<Parcel>()
    private var showingClients = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        bindScreen(binding.root, binding.tvStatus, binding.progress)
        showingClients = savedInstanceState?.getBoolean("clients") ?: false
        binding.btnCreatePackage.setOnClickListener {
            startActivity(Intent(this, ParcelEditorActivity::class.java))
        }
        binding.btnToggleSection.setOnClickListener {
            showingClients = !showingClients
            render()
        }
        binding.etFilter.doAfterTextChanged { render() }
        binding.btnLogout.setOnClickListener { logout() }
    }

    override suspend fun onAuthenticated(user: User) {
        clients = parcels.clients(user.id)
        packages = parcels.all(user.id)
        render()
    }

    private fun render() {
        binding.items.removeAllViews()
        binding.filterInput.isVisible = !showingClients
        binding.btnToggleSection.setText(if (showingClients) R.string.show_packages else R.string.show_clients)
        binding.tvSectionTitle.setText(if (showingClients) R.string.clients_title else R.string.packages_title)
        val count = if (showingClients) clients.size else packages.size
        binding.tvCount.text = resources.getQuantityString(
            if (showingClients) R.plurals.client_count else R.plurals.package_count, count, count)
        if (showingClients) {
            binding.tvEmpty.isVisible = clients.isEmpty()
            binding.tvEmpty.setText(R.string.empty_clients)
            clients.forEach { client ->
                val row = ItemClientBinding.inflate(layoutInflater, binding.items, false)
                row.tvName.text = getString(R.string.person_name, client.nombre, client.apellidos)
                row.tvInfo.text = getString(R.string.client_info, client.usuario,
                    resources.getQuantityString(R.plurals.age_years, client.edad, client.edad), client.telefono, client.direccion)
                binding.items.addView(row.root)
            }
        } else {
            val filter = binding.etFilter.text.toString().trim()
            val visible = packages.filter { parcel ->
                val client = clients.find { it.id == parcel.clienteId }
                listOf(parcel.codigo, parcel.descripcion, client?.usuario.orEmpty(),
                    client?.nombre.orEmpty(), client?.apellidos.orEmpty()).any { it.contains(filter, ignoreCase = true) }
            }
            binding.tvEmpty.isVisible = visible.isEmpty()
            binding.tvEmpty.setText(if (packages.isEmpty()) R.string.empty_packages else R.string.no_filter_results)
            visible.forEach { parcel ->
                val row = ItemParcelBinding.inflate(layoutInflater, binding.items, false)
                val client = clients.find { it.id == parcel.clienteId }
                row.tvCode.text = parcel.codigo
                row.tvDescription.text = parcel.descripcion
                row.tvClient.text = getString(R.string.recipient_summary,
                    client?.let { getString(R.string.client_selection, it.nombre, it.apellidos, it.usuario) }.orEmpty())
                row.tvState.text = parcel.estado
                row.btnEdit.setOnClickListener {
                    startActivity(Intent(this, ParcelEditorActivity::class.java).putExtra("parcelId", parcel.id))
                }
                binding.items.addView(row.root)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean("clients", showingClients)
        super.onSaveInstanceState(outState)
    }
}
