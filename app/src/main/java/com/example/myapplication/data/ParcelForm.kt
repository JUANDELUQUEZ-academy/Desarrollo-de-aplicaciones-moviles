package com.example.myapplication.data

import com.example.myapplication.data.local.ParcelStatus
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Locale

data class ParcelForm(
    val codigo: String, val clienteId: Long, val descripcion: String,
    val origen: String, val destino: String, val estado: String,
    val fechaEstimada: String, val notas: String
)

object ParcelValidator {
    fun normalizeCode(value: String): String = value.trim().uppercase(Locale.ROOT)

    fun validate(data: ParcelForm): Map<String, String> = buildMap {
        if (!normalizeCode(data.codigo).matches(Regex("[A-Z0-9][A-Z0-9-]{2,59}")))
            put("codigo", "Usa de 3 a 60 letras, números o guiones, comenzando con letra o número.")
        if (data.clienteId <= 0) put("cliente", "Selecciona un cliente registrado.")
        if (data.descripcion.trim().length !in 3..300) put("descripcion", "Escribe una descripción de 3 a 300 caracteres.")
        if (data.origen.trim().length !in 2..240) put("origen", "Escribe un origen de 2 a 240 caracteres.")
        if (data.destino.trim().length !in 2..240) put("destino", "Escribe un destino de 2 a 240 caracteres.")
        if (data.estado !in ParcelStatus.values) put("estado", "Selecciona un estado válido.")
        val date = data.fechaEstimada.trim()
        if (date.isNotEmpty()) {
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
            val position = ParsePosition(0)
            if (!date.matches(Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")) ||
                parser.parse(date, position) == null || position.index != date.length)
                put("fecha", "Usa una fecha válida con formato AAAA-MM-DD.")
        }
        if (data.notas.length > 500) put("notas", "Las notas admiten hasta 500 caracteres.")
    }
}

open class AppException(message: String) : Exception(message)
class AccessDeniedException : AppException("Inicia sesión con una cuenta autorizada.")
