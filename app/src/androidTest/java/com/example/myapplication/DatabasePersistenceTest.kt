package com.example.myapplication

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.Registration
import com.example.myapplication.data.DuplicateUserException
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DatabasePersistenceTest {
    @Test fun accountsSurviveReopeningAndDuplicatesAreRejected() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val name = "test-persistence-" + System.nanoTime() + ".db"
        fun open() = Room.databaseBuilder<AppDatabase>(context, name).setDriver(AndroidSQLiteDriver()).build()
        var db = open()
        try {
            val data = Registration("demo.persist", "Ana", "Perez", "18", "Ciudad prueba", "5551234567", "RutaDemo2026!", "RutaDemo2026!")
            val id = UserRepository(db.userDao()).register(data)
            db.close()
            db = open()
            val repository = UserRepository(db.userDao())
            assertEquals(id, repository.login(" DEMO.PERSIST ", data.password)?.id)
            assertNull(repository.login(data.usuario, "incorrecta"))
            var rejected = false
            try { repository.register(data) } catch (_: DuplicateUserException) { rejected = true }
            assertTrue(rejected)
            assertEquals(1, db.userDao().obtenerTodos().size)
        } finally {
            db.close()
            context.deleteDatabase(name)
        }
    }
}

