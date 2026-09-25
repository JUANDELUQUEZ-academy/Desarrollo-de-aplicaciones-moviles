package com.example.myapplication.data.local

import androidx.room3.Entity
import androidx.room3.ColumnInfo
import androidx.room3.Index
import androidx.room3.PrimaryKey

@Entity(tableName = "usuarios", indices = [Index(value = ["usuario"], unique = true)])
data class User(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val usuario: String,
    val nombre: String,
    val apellidos: String,
    val edad: Int,
    val direccion: String,
    val telefono: String,
    val passwordHash: ByteArray,
    val passwordSalt: ByteArray,
    @ColumnInfo(defaultValue = "'CLIENTE'") val rol: String = Roles.CLIENT
)

object Roles {
    const val CLIENT = "CLIENTE"
    const val ADMIN = "ADMINISTRADOR"
}

/** Datos de consulta del administrador, sin credenciales. */
data class ClientSummary(
    val id: Long, val usuario: String, val nombre: String, val apellidos: String,
    val edad: Int, val direccion: String, val telefono: String
)
