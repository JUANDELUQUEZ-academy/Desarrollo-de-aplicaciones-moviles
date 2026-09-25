package com.example.myapplication.data

import java.util.Locale

data class Registration(
    val usuario: String,
    val nombre: String,
    val apellidos: String,
    val edad: String,
    val direccion: String,
    val telefono: String,
    val password: String,
    val confirmation: String
)

object RegistrationValidator {
    fun normalizeUser(value: String): String = value.trim().lowercase(Locale.ROOT)

    fun validate(data: Registration, passwordOptional: Boolean = false): Map<String, String> = buildMap {
        if (!normalizeUser(data.usuario).matches(Regex("[a-z0-9._-]{3,30}")))
            put("usuario", "Usa de 3 a 30 letras, números, puntos o guiones.")
        if (data.nombre.isBlank()) put("nombre", "Escribe tu nombre.")
        if (data.apellidos.isBlank()) put("apellidos", "Escribe tus apellidos.")
        val age = data.edad.trim().toIntOrNull()
        if (age == null || age < 18 || age > 120)
            put("edad", "Debes tener entre 18 y 120 años.")
        if (data.direccion.isBlank()) put("direccion", "Escribe tu dirección o ubicación.")
        val phone = data.telefono.trim()
        val digits = phone.count(Char::isDigit)
        if (!phone.matches(Regex("[+0-9 ()-]+")) || digits !in 7..15)
            put("telefono", "Escribe un teléfono de 7 a 15 dígitos.")
        val changePassword = !passwordOptional || data.password.isNotEmpty() || data.confirmation.isNotEmpty()
        if (changePassword && (data.password.isBlank() || data.password.length !in 8..128))
            put("password", "Usa una contraseña de 8 a 128 caracteres.")
        if (changePassword && (data.confirmation.isEmpty() || data.password != data.confirmation))
            put("confirmation", "Las contraseñas deben coincidir exactamente.")
    }
}
