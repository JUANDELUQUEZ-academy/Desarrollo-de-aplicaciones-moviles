package com.example.myapplication.data

import androidx.room3.withWriteTransaction
import androidx.sqlite.SQLiteException
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.ClientSummary
import com.example.myapplication.data.local.Parcel
import com.example.myapplication.data.local.Roles

class ParcelRepository(private val db: AppDatabase) {
    private val users = UserRepository(db.userDao())

    suspend fun clients(actorId: Long): List<ClientSummary> {
        users.requireRole(actorId, Roles.ADMIN)
        return db.userDao().clientes()
    }

    suspend fun all(actorId: Long): List<Parcel> {
        users.requireRole(actorId, Roles.ADMIN)
        return db.parcelDao().activos()
    }

    suspend fun forAdmin(actorId: Long, id: Long): Parcel {
        users.requireRole(actorId, Roles.ADMIN)
        return db.parcelDao().porId(id) ?: throw AppException("Ese paquete no existe.")
    }

    suspend fun findForClient(actorId: Long, code: String): Parcel? {
        users.requireRole(actorId, Roles.CLIENT)
        return db.parcelDao().buscarPropio(ParcelValidator.normalizeCode(code), actorId)
    }

    suspend fun detailForClient(actorId: Long, id: Long): Parcel? {
        users.requireRole(actorId, Roles.CLIENT)
        return db.parcelDao().porId(id)?.takeIf { it.clienteId == actorId }
    }

    suspend fun save(actorId: Long, id: Long, form: ParcelForm): Long {
        val errors = ParcelValidator.validate(form)
        if (errors.isNotEmpty()) throw AppException(errors.values.first())
        val code = ParcelValidator.normalizeCode(form.codigo)
        return try {
            db.withWriteTransaction {
                users.requireRole(actorId, Roles.ADMIN)
                if (db.userDao().porId(form.clienteId)?.rol != Roles.CLIENT)
                    throw AppException("Selecciona un cliente registrado.")
                if (db.parcelDao().codigoExiste(code, id))
                    throw AppException("Ese código de rastreo ya existe. Usa otro.")
                val previous = if (id == 0L) null else forAdmin(actorId, id)
                val now = System.currentTimeMillis()
                val parcel = Parcel(
                    id = id, codigo = code, clienteId = form.clienteId,
                    descripcion = form.descripcion.trim(), origen = form.origen.trim(),
                    destino = form.destino.trim(), estado = form.estado,
                    fechaEstimada = form.fechaEstimada.trim(), notas = form.notas.trim(),
                    creadoEn = previous?.creadoEn ?: now, actualizadoEn = now,
                    creadoPor = previous?.creadoPor ?: actorId, actualizadoPor = actorId
                )
                if (id == 0L) db.parcelDao().insertar(parcel)
                else { db.parcelDao().actualizar(parcel); id }
            }
        } catch (error: SQLiteException) {
            if (db.parcelDao().codigoExiste(code, id)) throw AppException("Ese código de rastreo ya existe. Usa otro.")
            throw error
        }
    }

    suspend fun delete(actorId: Long, id: Long) = db.withWriteTransaction {
        val parcel = forAdmin(actorId, id)
        val now = System.currentTimeMillis()
        db.parcelDao().actualizar(parcel.copy(eliminadoEn = now, actualizadoEn = now, actualizadoPor = actorId))
    }
}
