package com.example.myapplication

import com.example.myapplication.data.ParcelForm
import com.example.myapplication.data.ParcelValidator
import com.example.myapplication.data.Registration
import com.example.myapplication.data.RegistrationValidator
import org.junit.Assert.*
import org.junit.Test

class ParcelValidatorTest {
    private val valid = ParcelForm("RP-001", 1, "Libros de texto", "Monterrey", "Ciudad de México", "Creado", "2028-02-29", "")

    @Test fun validPackageAndLeapDateAccepted() { assertTrue(ParcelValidator.validate(valid).isEmpty()) }
    @Test fun codeNormalizedAndInvalidCodesRejected() {
        assertEquals("RP-001", ParcelValidator.normalizeCode(" rp-001 "))
        listOf("", "AB", "-ABC", "ABC DEF", "A".repeat(61), "ÁBC").forEach {
            assertTrue("codigo" in ParcelValidator.validate(valid.copy(codigo = it)))
        }
    }
    @Test fun requiredFieldsAndClientAreValidated() {
        val errors = ParcelValidator.validate(valid.copy(clienteId = 0, descripcion = "", origen = " ", destino = ""))
        assertTrue(errors.keys.containsAll(listOf("cliente", "descripcion", "origen", "destino")))
    }
    @Test fun datesAreStrictAndOptional() {
        listOf("2026-02-29", "2026-13-01", "2026-04-31", "24/09/2026", "2026-1-1").forEach {
            assertTrue("fecha" in ParcelValidator.validate(valid.copy(fechaEstimada = it)))
        }
        assertTrue(ParcelValidator.validate(valid.copy(fechaEstimada = "")).isEmpty())
    }
    @Test fun statusAndLengthLimitsAreEnforced() {
        assertTrue("estado" in ParcelValidator.validate(valid.copy(estado = "DESCONOCIDO")))
        assertTrue("notas" in ParcelValidator.validate(valid.copy(notas = "a".repeat(501))))
        assertTrue("descripcion" in ParcelValidator.validate(valid.copy(descripcion = "a".repeat(301))))
    }
    @Test fun profileCanKeepPasswordButPartialChangeIsRejected() {
        val data = Registration("ana.demo", "Ana", "Pérez", "25", "Ciudad", "5551234567", "", "")
        assertTrue(RegistrationValidator.validate(data, passwordOptional = true).isEmpty())
        assertTrue("password" in RegistrationValidator.validate(data))
        assertTrue("confirmation" in RegistrationValidator.validate(data.copy(password = "OtraClave2026!"), true))
        assertTrue("password" in RegistrationValidator.validate(data.copy(confirmation = "OtraClave2026!"), true))
    }
}
