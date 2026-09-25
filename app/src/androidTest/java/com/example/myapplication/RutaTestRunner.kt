package com.example.myapplication

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import java.io.File

/** Mantiene los datos del usuario separados de los datos de las pruebas de interfaz. */
class RutaTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader, className: String, context: Context): Application =
        super.newApplication(cl, RutaTestApplication::class.java.name, context)
}

class RutaTestApplication : Application() {
    override fun getDatabasePath(name: String): File =
        super.getDatabasePath(if (name == "rutapaquete.db") TEST_DATABASE else name)

    override fun onCreate() {
        super.onCreate()
        // Esta base pertenece exclusivamente a las pruebas, nunca a la ejecución normal.
        deleteDatabase(TEST_DATABASE)
    }

    companion object { const val TEST_DATABASE = "rutapaquete-instrumented.db" }
}
