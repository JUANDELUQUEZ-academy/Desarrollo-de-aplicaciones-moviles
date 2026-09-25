package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.lifecycle.Lifecycle
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.onData
import androidx.test.espresso.NoMatchingViewException
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.myapplication.data.Registration
import com.example.myapplication.data.UserRepository
import com.example.myapplication.data.local.AppDatabase
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

@RunWith(AndroidJUnit4::class)
class WorkspaceFlowTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val password = "RutaClave2026!"

    private fun waitFor(check: () -> Unit) {
        val deadline = SystemClock.elapsedRealtime() + 60000
        var failure: Throwable? = null
        while (SystemClock.elapsedRealtime() < deadline) {
            try { check(); return }
            catch (error: AssertionError) { failure = error }
            catch (error: NoMatchingViewException) { failure = error }
            SystemClock.sleep(150)
        }
        throw AssertionError("No terminó la operación", failure)
    }
    private fun ready(id: Int) = waitFor { onView(withId(id)).check(matches(isEnabled())) }
    private fun tap(id: Int) { ready(id); onView(withId(id)).perform(scrollTo(), click()) }
    private fun fill(id: Int, value: String) { ready(id); onView(withId(id)).perform(scrollTo(), replaceText(value), closeSoftKeyboard()) }
    private fun select(id: Int, value: String) {
        tap(id)
        onData(equalTo(value)).perform(click())
    }
    private fun shot(name: String) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync()
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        val directory = File(context.getExternalFilesDir(null), "evidencias/v2").apply { mkdirs() }
        File(directory, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bitmap.recycle()
    }
    private fun login(username: String, secret: String = password, target: Int) {
        fill(R.id.etUsername, username)
        fill(R.id.etPassword, secret)
        tap(R.id.btnLogin)
        ready(target)
    }
    private fun search(code: String) {
        fill(R.id.etTrackingCode, code)
        tap(R.id.btnSearch)
    }
    private fun missing() = waitFor {
        onView(withId(R.id.tvStatus)).check(matches(withText(R.string.package_missing)))
    }

    @Test fun administratorCrudClientTrackingAndProfile() {
        val dao = AppDatabase.getInstance(context).userDao()
        runBlocking {
            val repository = UserRepository(dao)
            repository.register(Registration("client.alpha", "Ana", "Pérez", "25", "Monterrey", "5551234567", password, password))
            repository.register(Registration("client.beta", "Luis", "García", "30", "Puebla", "5557654321", password, password))
        }
        Session.end()
        ActivityScenario.launch(MainActivity::class.java).use {
            waitFor { onView(withId(R.id.btnSetupAdmin)).check(matches(withEffectiveVisibility(Visibility.VISIBLE))) }
            tap(R.id.btnSetupAdmin)
            fill(R.id.etUsername, "admin.local")
            fill(R.id.etName, "Administrador")
            fill(R.id.etSurname, "Local")
            fill(R.id.etAge, "30")
            fill(R.id.etAddress, "Oficina central")
            fill(R.id.etPhone, "5550001122")
            fill(R.id.etPassword, password)
            fill(R.id.etConfirm, password)
            tap(R.id.btnSubmit)
            waitFor { onView(withId(R.id.tvStatus)).check(matches(withText(R.string.admin_created))) }
            login("admin.local", target = R.id.btnCreatePackage)
            shot("01-administracion")
            tap(R.id.btnToggleSection)
            onView(withText("Ana Pérez")).perform(scrollTo()).check(matches(isDisplayed()))
            shot("02-clientes")
            tap(R.id.btnToggleSection)
            tap(R.id.btnCreatePackage)
            ready(R.id.btnSave)
            tap(R.id.btnSave)
            onView(withId(R.id.etCode)).check(matches(hasFocus()))
            fill(R.id.etCode, "rp-2026-001")
            select(R.id.spClient, "Ana Pérez (@client.alpha)")
            fill(R.id.etDescription, "Libros de programación")
            fill(R.id.etOrigin, "Monterrey")
            fill(R.id.etDestination, "Puebla")
            fill(R.id.etEstimated, "2026-12-20")
            fill(R.id.etNotes, "Entregar en recepción.")
            onView(withId(R.id.etCode)).perform(scrollTo())
            shot("03-alta-paquete")
            tap(R.id.btnSave)
            ready(R.id.btnCreatePackage)
            onView(withText("RP-2026-001")).perform(scrollTo()).check(matches(isDisplayed()))
            shot("04-paquete-creado")
            // El destinatario y los cambios pendientes deben sobrevivir a pausa y recreación.
            val parcelId = runBlocking { AppDatabase.getInstance(context).parcelDao().activos().single().id }
            ActivityScenario.launch<ParcelEditorActivity>(Intent(context, ParcelEditorActivity::class.java)
                .putExtra("parcelId", parcelId)).use { editor ->
                ready(R.id.btnSave)
                select(R.id.spClient, "Luis García (@client.beta)")
                fill(R.id.etDescription, "Cambios pendientes")
                editor.moveToState(Lifecycle.State.CREATED)
                editor.moveToState(Lifecycle.State.RESUMED)
                ready(R.id.btnSave)
                onView(withId(R.id.spClient)).check(matches(withSpinnerText("Luis García (@client.beta)")))
                onView(withId(R.id.etDescription)).check(matches(withText("Cambios pendientes")))
                editor.recreate()
                ready(R.id.btnSave)
                onView(withId(R.id.spClient)).check(matches(withSpinnerText("Luis García (@client.beta)")))
                onView(withId(R.id.etDescription)).check(matches(withText("Cambios pendientes")))
                tap(R.id.btnCancel)
            }
            val unchanged = runBlocking { AppDatabase.getInstance(context).parcelDao().porId(parcelId)!! }
            assertEquals("Libros de programación", unchanged.descripcion)
            assertEquals(runBlocking { dao.buscarPorUsuario("client.alpha")!!.id }, unchanged.clienteId)
            // ActivityScenario inicia una tarea limpia: restaurar el panel para continuar el recorrido.
            InstrumentationRegistry.getInstrumentation().startActivitySync(
                Intent(context, AdminActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            ready(R.id.btnCreatePackage)
            tap(R.id.btnEdit)
            ready(R.id.btnSave)
            select(R.id.spStatus, "En tránsito")
            fill(R.id.etNotes, "En camino al centro de distribución.")
            tap(R.id.btnSave)
            ready(R.id.btnLogout)
            tap(R.id.btnLogout)

            login("client.beta", target = R.id.btnSearch)
            search("RP-2026-001")
            missing()
            shot("05-paquete-ajeno")
            tap(R.id.btnLogout)
            login("client.alpha", target = R.id.btnSearch)
            search(" rp-2026-001 ")
            ready(R.id.btnBack)
            onView(withId(R.id.tvCode)).perform(scrollTo()).check(matches(withText("RP-2026-001")))
            onView(withId(R.id.tvSummary)).check(matches(withSubstring("En tránsito")))
            shot("06-detalle-cliente")
            tap(R.id.btnBack)
            tap(R.id.btnProfile)
            ready(R.id.btnSaveProfile)
            fill(R.id.etName, "Ana María")
            fill(R.id.etUsername, "client.updated")
            fill(R.id.etAddress, "Nueva dirección 123")
            fill(R.id.etPhone, "5551112233")
            fill(R.id.etCurrentPassword, password)
            fill(R.id.etPassword, "OtraClave2026!")
            fill(R.id.etConfirm, "OtraClave2026!")
            onView(withId(R.id.etName)).perform(scrollTo())
            shot("07-editar-perfil")
            tap(R.id.btnSaveProfile)
            ready(R.id.btnProfile)
            waitFor { onView(withId(R.id.tvWelcome)).check(matches(withText("Hola, Ana María"))) }
            tap(R.id.btnLogout)
            login("client.updated", "OtraClave2026!", R.id.btnSearch)
            search("RP-2026-001")
            ready(R.id.btnBack)
            onView(withId(R.id.tvRecipient)).check(matches(withText("Destinatario: Ana María Pérez")))
            tap(R.id.btnBack)
            tap(R.id.btnLogout)

            login("admin.local", target = R.id.btnCreatePackage)
            tap(R.id.btnEdit)
            ready(R.id.btnSave)
            select(R.id.spClient, "Luis García (@client.beta)")
            tap(R.id.btnSave)
            ready(R.id.btnLogout)
            tap(R.id.btnLogout)
            login("client.updated", "OtraClave2026!", R.id.btnSearch)
            search("RP-2026-001")
            missing()
            tap(R.id.btnLogout)
            login("client.beta", target = R.id.btnSearch)
            search("RP-2026-001")
            ready(R.id.btnBack)
            onView(withId(R.id.tvRecipient)).check(matches(withText("Destinatario: Luis García")))
            shot("08-reasignacion")
            tap(R.id.btnBack)
            tap(R.id.btnLogout)

            login("admin.local", target = R.id.btnCreatePackage)
            tap(R.id.btnEdit)
            ready(R.id.btnDelete)
            tap(R.id.btnDelete)
            onView(withText(R.string.delete_title)).check(matches(isDisplayed()))
            shot("09-confirmacion-eliminar")
            onView(withId(android.R.id.button2)).perform(click()) // cancelar conserva el paquete
            tap(R.id.btnDelete)
            onView(withId(android.R.id.button1)).perform(click())
            ready(R.id.btnCreatePackage)
            onView(withId(R.id.tvEmpty)).check(matches(withText(R.string.empty_packages)))
            shot("10-paquete-eliminado")
            tap(R.id.btnLogout)
            login("client.beta", target = R.id.btnSearch)
            search("RP-2026-001")
            missing()
            tap(R.id.btnLogout)
            assertNull(Session.userId)
            assertTrue(runBlocking { dao.clientes() }.any { it.usuario == "client.updated" })
        }
    }

    @Test fun clientCannotOpenAdministratorScreen() {
        val account = runBlocking {
            val repository = UserRepository(AppDatabase.getInstance(context).userDao())
            repository.register(Registration("guard.client", "Cliente", "Prueba", "22", "Ciudad", "5551234567", password, password))
        }
        Session.start(account)
        ActivityScenario.launch(AdminActivity::class.java).use {
            waitFor { onView(withId(R.id.btnLogin)).check(matches(isDisplayed())) }
            assertNull(Session.userId)
        }
    }
}
