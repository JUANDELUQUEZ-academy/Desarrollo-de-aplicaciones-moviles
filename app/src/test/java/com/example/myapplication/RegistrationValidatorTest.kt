package com.example.myapplication

import com.example.myapplication.data.Registration
import com.example.myapplication.data.RegistrationValidator
import org.junit.Assert.*
import org.junit.Test

class RegistrationValidatorTest {
    private val valid = Registration("demo.ruta", "Ana", "Pérez", "18", "Ciudad de prueba", "5551234567", "RutaDemo2026!", "RutaDemo2026!")
    @Test fun adultAccepted() { assertTrue(RegistrationValidator.validate(valid).isEmpty()) }
    @Test fun minorRejected() { assertTrue("edad" in RegistrationValidator.validate(valid.copy(edad = "17"))) }
    @Test fun invalidAgesRejected() {
        listOf("", "-1", "18.5", "abc", "999999999999", "121").forEach {
            assertTrue("edad" in RegistrationValidator.validate(valid.copy(edad = it)))
        }
    }
    @Test fun emptyFieldsRejected() {
        assertEquals(8, RegistrationValidator.validate(Registration("", "", "", "", "", "", "", "")).size)
    }
    @Test fun passwordComparisonIsExact() {
        assertTrue("confirmation" in RegistrationValidator.validate(valid.copy(confirmation = "rutademo2026!")))
        assertTrue("confirmation" in RegistrationValidator.validate(valid.copy(confirmation = valid.password + " ")))
    }
    @Test fun shortPasswordRejected() {
        assertTrue("password" in RegistrationValidator.validate(valid.copy(password = "1234567", confirmation = "1234567")))
    }
    @Test fun blankNamesRejected() {
        assertTrue("nombre" in RegistrationValidator.validate(valid.copy(nombre = "   ")))
    }
    @Test fun phoneValidated() {
        assertTrue("telefono" in RegistrationValidator.validate(valid.copy(telefono = "abc5551234567")))
        assertTrue(RegistrationValidator.validate(valid.copy(telefono = "+52 (555) 123-4567")).isEmpty())
    }
    @Test fun usernameNormalized() { assertEquals("demo.ruta", RegistrationValidator.normalizeUser(" DEMO.Ruta ")) }
    @Test fun accentsAndSpacesAcceptedInNames() {
        assertTrue(RegistrationValidator.validate(valid.copy(nombre = "María José", apellidos = "De la Peña")).isEmpty())
    }
}

