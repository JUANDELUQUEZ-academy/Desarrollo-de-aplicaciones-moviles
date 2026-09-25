package com.example.myapplication.data.local

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "paquetes", indices = [Index(value = ["codigo"], unique = true), Index(value = ["clienteId"])],
    foreignKeys = [ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["clienteId"], onDelete = ForeignKey.RESTRICT)])
data class Parcel(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val codigo: String,
    val clienteId: Long,
    val descripcion: String,
    val origen: String,
    val destino: String,
    val estado: String,
    val fechaEstimada: String,
    val notas: String,
    val creadoEn: Long,
    val actualizadoEn: Long,
    val creadoPor: Long,
    val actualizadoPor: Long,
    val eliminadoEn: Long? = null
)

object ParcelStatus {
    val values = listOf("Creado", "En tránsito", "En reparto", "Entregado", "Incidencia")
}
