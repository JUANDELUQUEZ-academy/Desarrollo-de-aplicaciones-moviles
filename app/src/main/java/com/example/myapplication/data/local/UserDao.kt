package com.example.myapplication.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Update

@Dao
interface UserDao {
    @Insert
    suspend fun insertar(user: User): Long

    @Query("SELECT * FROM usuarios WHERE usuario = :usuario LIMIT 1")
    suspend fun buscarPorUsuario(usuario: String): User?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE usuario = :usuario)")
    suspend fun existe(usuario: String): Boolean

    @Query("SELECT * FROM usuarios ORDER BY id")
    suspend fun obtenerTodos(): List<User>

    @Query("SELECT * FROM usuarios WHERE id = :id LIMIT 1")
    suspend fun porId(id: Long): User?

    @Query("SELECT EXISTS(SELECT 1 FROM usuarios WHERE rol = 'ADMINISTRADOR')")
    suspend fun hayAdministrador(): Boolean

    @Query("SELECT id, usuario, nombre, apellidos, edad, direccion, telefono FROM usuarios WHERE rol = 'CLIENTE' ORDER BY nombre, apellidos, id")
    suspend fun clientes(): List<ClientSummary>

    @Update
    suspend fun actualizar(user: User): Int

    @Transaction
    suspend fun crearPrimerAdministrador(user: User): Long {
        if (hayAdministrador()) throw com.example.myapplication.data.AppException("El administrador ya está configurado.")
        return insertar(user)
    }
}
