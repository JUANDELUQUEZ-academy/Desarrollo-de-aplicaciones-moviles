package com.example.myapplication.data

import androidx.sqlite.SQLiteException
import com.example.myapplication.data.local.User
import com.example.myapplication.data.local.UserDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuplicateUserException : Exception("El usuario ya existe.")

class UserRepository(private val dao: UserDao) {
    suspend fun register(data: Registration): Long {
        require(RegistrationValidator.validate(data).isEmpty()) { "Datos de registro inválidos." }
        val username = RegistrationValidator.normalizeUser(data.usuario)
        if (dao.existe(username)) throw DuplicateUserException()
        val salt = PasswordHasher.newSalt()
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(data.password, salt) }
        val user = User(
            usuario = username, nombre = data.nombre.trim(), apellidos = data.apellidos.trim(),
            edad = data.edad.trim().toInt(), direccion = data.direccion.trim(),
            telefono = data.telefono.trim(), passwordHash = hash, passwordSalt = salt
        )
        return try {
            dao.insertar(user)
        } catch (error: SQLiteException) {
            // El índice UNIQUE también protege contra inserciones concurrentes.
            if (dao.existe(username)) throw DuplicateUserException()
            throw error
        }
    }

    suspend fun login(username: String, password: String): User? {
        val user = dao.buscarPorUsuario(RegistrationValidator.normalizeUser(username)) ?: return null
        return if (withContext(Dispatchers.Default) {
            PasswordHasher.matches(password, user.passwordSalt, user.passwordHash)
        }) user else null
    }
}

