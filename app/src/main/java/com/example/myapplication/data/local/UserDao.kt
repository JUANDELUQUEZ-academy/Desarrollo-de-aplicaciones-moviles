package com.example.myapplication.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.Query

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
}

