package com.example.myapplication.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface ParcelDao {
    @Insert suspend fun insertar(parcel: Parcel): Long
    @Update suspend fun actualizar(parcel: Parcel): Int

    @Query("SELECT * FROM paquetes WHERE eliminadoEn IS NULL ORDER BY actualizadoEn DESC, id DESC")
    suspend fun activos(): List<Parcel>

    @Query("SELECT * FROM paquetes WHERE id = :id AND eliminadoEn IS NULL LIMIT 1")
    suspend fun porId(id: Long): Parcel?

    @Query("SELECT * FROM paquetes WHERE codigo = :codigo AND clienteId = :clienteId AND eliminadoEn IS NULL LIMIT 1")
    suspend fun buscarPropio(codigo: String, clienteId: Long): Parcel?

    @Query("SELECT EXISTS(SELECT 1 FROM paquetes WHERE codigo = :codigo AND id != :exceptoId)")
    suspend fun codigoExiste(codigo: String, exceptoId: Long = 0): Boolean
}
