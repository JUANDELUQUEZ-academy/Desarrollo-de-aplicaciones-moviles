package com.example.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class AccountFlowTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val username = "demo." + System.currentTimeMillis()
    private val password = "RutaDemo2026!"

    private fun shot(name: String) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val dir = File(context.getExternalFilesDir(null), "evidencias").apply { mkdirs() }
        File(dir, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }

    private fun waitFor(check: () -> Unit) {
        val deadline = SystemClock.elapsedRealtime() + 45000
        var failure: Throwable? = null
        while (SystemClock.elapsedRealtime() < deadline) {
            try { check(); return } catch (error: AssertionError) { failure = error }
            catch (error: androidx.test.espresso.NoMatchingViewException) { failure = error }
            SystemClock.sleep(200)
        }
        throw AssertionError("No terminó la operación asíncrona", failure)
    }

    private fun fill(id: Int, value: String) {
        onView(withId(id)).perform(scrollTo(), replaceText(value), closeSoftKeyboard())
    }

    @Test fun registerValidateLoginAndTrack() {
        Session.end()
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        try {
            shot("01-inicio")
            onView(withId(R.id.btnCreateAccount)).perform(scrollTo(), click())
            shot("02a-formulario")
            // Verificar el formulario real con IME visible, no solo sin teclado.
            onView(withId(R.id.etUsername)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.etUsername)).check(matches(hasVisibleKeyboard())) }
            onView(withId(R.id.etUsername)).check(matches(isCompletelyDisplayed()))
            shot("02b-formulario-teclado")
            onView(withId(R.id.btnSubmit)).perform(scrollTo()).check(matches(isCompletelyDisplayed()))
            onView(withId(R.id.btnSubmit)).check(matches(hasVisibleKeyboard()))
            shot("02c-botones-teclado")
            onView(withId(R.id.btnSubmit)).perform(closeSoftKeyboard())
            onView(withId(R.id.btnSubmit)).perform(scrollTo(), click())
            onView(withId(R.id.etUsername)).check(matches(hasErrorInParent()))
            shot("02-campos-obligatorios")

            fill(R.id.etUsername, username)
            fill(R.id.etName, "Ana")
            fill(R.id.etSurname, "Perez")
            fill(R.id.etAge, "17")
            fill(R.id.etAddress, "Ciudad de prueba")
            fill(R.id.etPhone, "5551234567")
            fill(R.id.etPassword, password)
            fill(R.id.etConfirm, password)
            onView(withId(R.id.btnSubmit)).perform(scrollTo(), click())
            onView(withId(R.id.etAge)).perform(scrollTo()).check(matches(hasErrorInParent()))
            shot("03-edad-menor")
            fill(R.id.etAge, "18")
            fill(R.id.etConfirm, "Diferente2026!")
            onView(withId(R.id.btnSubmit)).perform(scrollTo(), click())
            onView(withId(R.id.etConfirm)).check(matches(hasErrorInParent()))
            shot("04-contrasenas")
            fill(R.id.etConfirm, password)
            onView(withId(R.id.btnSubmit)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.tvStatus)).check(matches(withText(R.string.registration_success))) }
            shot("05-registro-exitoso")
            val users = runBlocking { AppDatabase.getInstance(context).userDao().obtenerTodos() }
            val saved = users.single { it.usuario == username }
            assertEquals("Ana", saved.nombre)
            assertEquals("Perez", saved.apellidos)
            assertEquals("Ciudad de prueba", saved.direccion)
            assertEquals("5551234567", saved.telefono)
            assertEquals(18, saved.edad)
            assertTrue(saved.id > 0)
            assertEquals(32, saved.passwordHash.size)

            // Cancelar un formulario con datos no debe crear ninguna cuenta.
            onView(withId(R.id.btnCreateAccount)).perform(scrollTo(), click())
            fill(R.id.etUsername, "cancel." + System.currentTimeMillis())
            fill(R.id.etName, "No guardar")
            onView(withId(R.id.btnCancel)).perform(scrollTo(), click())
            assertEquals(users.size, runBlocking { AppDatabase.getInstance(context).userDao().obtenerTodos() }.size)

            onView(withId(R.id.btnCreateAccount)).perform(scrollTo(), click())
            fill(R.id.etUsername, username)
            fill(R.id.etName, "Ana")
            fill(R.id.etSurname, "Perez")
            fill(R.id.etAge, "18")
            fill(R.id.etAddress, "Ciudad de prueba")
            fill(R.id.etPhone, "5551234567")
            fill(R.id.etPassword, password)
            fill(R.id.etConfirm, password)
            onView(withId(R.id.btnSubmit)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.etUsername)).check(matches(hasErrorInParent())) }
            onView(withId(R.id.etUsername)).perform(scrollTo())
            shot("06-usuario-duplicado")
            onView(withId(R.id.btnCancel)).perform(scrollTo(), click())
            assertEquals(1, runBlocking { AppDatabase.getInstance(context).userDao().obtenerTodos() }.count { it.usuario == username })

            fill(R.id.etPassword, "incorrecta")
            onView(withId(R.id.btnLogin)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.tvStatus)).check(matches(withText(R.string.invalid_credentials))) }
            shot("07-login-invalido")
            fill(R.id.etPassword, password)
            onView(withId(R.id.btnLogin)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.btnSearch)).check(matches(org.hamcrest.Matchers.allOf(isDisplayed(), isEnabled()))) }
            shot("08-rastreo")
            fill(R.id.etTrackingCode, "PAQ123456")
            onView(withId(R.id.btnSearch)).perform(scrollTo(), click())
            waitFor { onView(withId(R.id.btnLogout)).check(matches(isEnabled())) }
            onView(withId(R.id.btnSearch)).check(matches(isDisplayed()))
            onView(withId(R.id.btnLogout)).perform(scrollTo(), click())
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
            assertNull(Session.userId)
            File(context.getExternalFilesDir(null), "evidencias/usuario-prueba.txt").writeText(username)
        } finally { scenario.close() }
    }

    @Test fun idleLoginRecreationPreservesUsername() {
        Session.end()
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            fill(R.id.etUsername, "demo.recreacion")
            // No hay autenticación pendiente: se recrea la pantalla en reposo.
            scenario.recreate()
            onView(withId(R.id.etUsername)).check(matches(withText("demo.recreacion")))
            onView(withId(R.id.btnLogin)).perform(scrollTo()).check(matches(isDisplayed()))
            assertNull(Session.userId)
            shot("09-inicio-recreado")
        }
    }

    private fun hasVisibleKeyboard(): org.hamcrest.Matcher<android.view.View> =
        object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("teclado de pantalla visible en los insets de la ventana")
            }
            override fun matchesSafely(view: android.view.View): Boolean =
                ViewCompat.getRootWindowInsets(view)?.isVisible(WindowInsetsCompat.Type.ime()) == true
        }

    private fun hasErrorInParent(): org.hamcrest.Matcher<android.view.View> =
        object : org.hamcrest.TypeSafeMatcher<android.view.View>() {
            override fun describeTo(description: org.hamcrest.Description) {
                description.appendText("TextInputLayout con error")
            }
            override fun matchesSafely(view: android.view.View): Boolean {
                var parent = view.parent
                while (parent != null) {
                    if (parent is com.google.android.material.textfield.TextInputLayout) return !parent.error.isNullOrEmpty()
                    parent = parent.parent
                }
                return false
            }
        }
}
