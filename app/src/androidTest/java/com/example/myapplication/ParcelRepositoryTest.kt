package com.example.myapplication

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.*
import com.example.myapplication.data.local.*
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ParcelRepositoryTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val password = "PruebaRuta2026!"
    private fun registration(username: String) = Registration(username, "Ana", "Pérez", "25",
        "Ciudad de prueba", "5551234567", password, password)

    private suspend fun rejected(block: suspend () -> Unit) {
        var error: AppException? = null
        try { block() } catch (failure: AppException) { error = failure }
        assertNotNull("La operación debe ser rechazada", error)
    }

    @Test fun rolesCrudOwnershipAndProfilePersist() = runBlocking {
        val name = "test-crud-" + System.nanoTime() + ".db"
        fun open() = Room.databaseBuilder<AppDatabase>(context, name)
            .setDriver(AndroidSQLiteDriver()).addMigrations(AppDatabase.MIGRATION_1_2).build()
        var db = open()
        try {
            var accounts = UserRepository(db.userDao())
            val admin = accounts.register(registration("admin.test"), administratorSetup = true)
            val first = accounts.register(registration("client.one"))
            val second = accounts.register(registration("client.two"))
            assertEquals(Roles.ADMIN, accounts.login("admin.test", password)?.rol)
            assertEquals(Roles.CLIENT, accounts.login("client.one", password)?.rol)
            rejected { accounts.register(registration("admin.two"), administratorSetup = true) }
            var repository = ParcelRepository(db)
            assertEquals(2, repository.clients(admin).size)
            val form = ParcelForm("rp-001", first, "Libros", "Monterrey", "Puebla", "Creado", "2026-12-20", "Frágil")
            rejected { repository.save(first, 0, form) }
            rejected { repository.all(first) }
            rejected { repository.clients(second) }
            rejected { repository.save(admin, 0, form.copy(clienteId = admin)) }
            rejected { repository.save(admin, 0, form.copy(clienteId = 999999)) }
            val parcelId = repository.save(admin, 0, form)
            assertEquals("RP-001", repository.findForClient(first, " rp-001 ")?.codigo)
            assertNull(repository.findForClient(second, "RP-001"))
            assertNull(repository.detailForClient(second, parcelId))
            assertNull(repository.findForClient(first, "NO-EXISTE"))
            rejected { repository.save(admin, 0, form.copy(codigo = " RP-001 ")) }
            rejected { repository.delete(first, parcelId) }
            rejected { accounts.updateProfile(first, registration("client.new"), "incorrecta") }
            rejected { accounts.updateProfile(first, registration("client.two"), password) }
            accounts.updateProfile(first, registration("client.new").copy(nombre = "Ana María", password = "", confirmation = ""), password)
            assertNull(accounts.login("client.one", password))
            assertEquals(first, accounts.login("CLIENT.NEW", password)?.id)
            assertEquals(parcelId, repository.findForClient(first, "RP-001")?.id)
            val newPassword = "NuevaClave2026!"
            accounts.updateProfile(first, registration("client.new").copy(password = newPassword, confirmation = newPassword), password)
            assertNull(accounts.login("client.new", password))
            assertEquals(first, accounts.login("client.new", newPassword)?.id)
            repository.save(admin, parcelId, form.copy(clienteId = second, estado = "En tránsito"))
            assertNull(repository.findForClient(first, "RP-001"))
            assertEquals("En tránsito", repository.findForClient(second, "RP-001")?.estado)
            db.close()
            db = open()
            accounts = UserRepository(db.userDao())
            repository = ParcelRepository(db)
            assertEquals(first, accounts.login("client.new", newPassword)?.id)
            assertEquals(second, repository.forAdmin(admin, parcelId).clienteId)
            repository.delete(admin, parcelId)
            assertTrue(repository.all(admin).isEmpty())
            assertNull(repository.findForClient(second, "RP-001"))
            assertNull(repository.detailForClient(second, parcelId))
            rejected { repository.save(admin, 0, form) } // un código retirado tampoco se reutiliza
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }

    @Test fun migrationPreservesVersionOneAccountsAndCredentials() = runBlocking {
        val name = "test-migration-" + System.nanoTime() + ".db"
        val file = context.getDatabasePath(name)
        file.parentFile!!.mkdirs()
        val salt = PasswordHasher.newSalt()
        val hash = PasswordHasher.hash(password, salt)
        SQLiteDatabase.openOrCreateDatabase(file, null).use { legacy ->
            legacy.execSQL("CREATE TABLE usuarios (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, usuario TEXT NOT NULL, nombre TEXT NOT NULL, apellidos TEXT NOT NULL, edad INTEGER NOT NULL, direccion TEXT NOT NULL, telefono TEXT NOT NULL, passwordHash BLOB NOT NULL, passwordSalt BLOB NOT NULL)")
            legacy.execSQL("CREATE UNIQUE INDEX index_usuarios_usuario ON usuarios(usuario)")
            legacy.execSQL("INSERT INTO usuarios (id, usuario, nombre, apellidos, edad, direccion, telefono, passwordHash, passwordSalt) VALUES (42, 'legacy.user', 'Ana', 'Pérez', 25, 'Ciudad', '5551234567', ?, ?)", arrayOf(hash, salt))
            legacy.version = 1
        }
        val db = Room.databaseBuilder<AppDatabase>(context, name).setDriver(AndroidSQLiteDriver())
            .addMigrations(AppDatabase.MIGRATION_1_2).build()
        try {
            val account = UserRepository(db.userDao()).login("legacy.user", password)!!
            assertEquals(42L, account.id)
            assertEquals(Roles.CLIENT, account.rol)
            assertArrayEquals(hash, account.passwordHash)
            assertArrayEquals(salt, account.passwordSalt)
            assertFalse(db.userDao().hayAdministrador())
            assertTrue(db.parcelDao().activos().isEmpty())
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}
