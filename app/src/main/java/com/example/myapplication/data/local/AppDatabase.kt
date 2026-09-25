package com.example.myapplication.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

@Database(entities = [User::class, Parcel::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun parcelDao(): ParcelDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override suspend fun migrate(connection: SQLiteConnection) {
                connection.execSQL("ALTER TABLE usuarios ADD COLUMN rol TEXT NOT NULL DEFAULT 'CLIENTE'")
                connection.execSQL("""
                    CREATE TABLE IF NOT EXISTS paquetes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        codigo TEXT NOT NULL, clienteId INTEGER NOT NULL,
                        descripcion TEXT NOT NULL, origen TEXT NOT NULL, destino TEXT NOT NULL,
                        estado TEXT NOT NULL, fechaEstimada TEXT NOT NULL, notas TEXT NOT NULL,
                        creadoEn INTEGER NOT NULL, actualizadoEn INTEGER NOT NULL,
                        creadoPor INTEGER NOT NULL, actualizadoPor INTEGER NOT NULL, eliminadoEn INTEGER,
                        FOREIGN KEY(clienteId) REFERENCES usuarios(id) ON UPDATE NO ACTION ON DELETE RESTRICT
                    )
                """.trimIndent())
                connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_paquetes_codigo ON paquetes(codigo)")
                connection.execSQL("CREATE INDEX IF NOT EXISTS index_paquetes_clienteId ON paquetes(clienteId)")
            }
        }
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<AppDatabase>(
                    context.applicationContext, "rutapaquete.db"
                ).setDriver(AndroidSQLiteDriver()).addMigrations(MIGRATION_1_2).build().also { instance = it }
            }
    }
}
