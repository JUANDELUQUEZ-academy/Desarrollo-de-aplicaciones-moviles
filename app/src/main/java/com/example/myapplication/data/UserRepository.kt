package com.example.myapplication.data

import androidx.sqlite.SQLiteException
import com.example.myapplication.data.local.User
import com.example.myapplication.data.local.UserDao
import com.example.myapplication.data.local.Roles
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DuplicateUserException : AppException("Este usuario ya está registrado.")

class UserRepository(private val dao: UserDao) {
    suspend fun register(data: Registration, administratorSetup: Boolean = false): Long {
        require(RegistrationValidator.validate(data).isEmpty()) { "Datos de registro inválidos." }
        if (administratorSetup && dao.hayAdministrador()) throw AppException("El administrador ya está configurado.")
        val username = RegistrationValidator.normalizeUser(data.usuario)
        if (dao.existe(username)) throw DuplicateUserException()
        val salt = PasswordHasher.newSalt()
        val hash = withContext(Dispatchers.Default) { PasswordHasher.hash(data.password, salt) }
        val user = User(
            usuario = username, nombre = data.nombre.trim(), apellidos = data.apellidos.trim(),
            edad = data.edad.trim().toInt(), direccion = data.direccion.trim(),
            telefono = data.telefono.trim(), passwordHash = hash, passwordSalt = salt,
            rol = if (administratorSetup) Roles.ADMIN else Roles.CLIENT
        )
        return try {
            if (administratorSetup) dao.crearPrimerAdministrador(user) else dao.insertar(user)
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

    suspend fun requireRole(id: Long, role: String): User =
        dao.porId(id)?.takeIf { it.rol == role } ?: throw AccessDeniedException()

    suspend fun updateProfile(id: Long, data: Registration, currentPassword: String) {
        val user = requireRole(id, Roles.CLIENT)
        val errors = RegistrationValidator.validate(data, passwordOptional = true)
        if (errors.isNotEmpty()) throw AppException(errors.values.first())
        if (!withContext(Dispatchers.Default) {
            PasswordHasher.matches(currentPassword, user.passwordSalt, user.passwordHash)
        }) throw AppException("La contraseña actual es incorrecta.")
        val username = RegistrationValidator.normalizeUser(data.usuario)
        val existing = dao.buscarPorUsuario(username)
        if (existing != null && existing.id != id) throw DuplicateUserException()
        val salt = if (data.password.isEmpty()) user.passwordSalt else PasswordHasher.newSalt()
        val hash = if (data.password.isEmpty()) user.passwordHash else withContext(Dispatchers.Default) {
            PasswordHasher.hash(data.password, salt)
        }
        try {
            dao.actualizar(user.copy(
                usuario = username, nombre = data.nombre.trim(), apellidos = data.apellidos.trim(),
                edad = data.edad.trim().toInt(), direccion = data.direccion.trim(),
                telefono = data.telefono.trim(), passwordHash = hash, passwordSalt = salt
            ))
        } catch (error: SQLiteException) {
            if (dao.buscarPorUsuario(username)?.id?.let { it != id } == true) throw DuplicateUserException()
            throw error
        }
    }
}
