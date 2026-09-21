package com.example.myapplication

import com.example.myapplication.data.PasswordHasher
import org.junit.Assert.*
import org.junit.Test

class PasswordHasherTest {
    @Test fun saltAndVerification() {
        val salt = PasswordHasher.newSalt()
        val other = PasswordHasher.newSalt()
        assertFalse(salt.contentEquals(other))
        val hash = PasswordHasher.hash("RutaDemo2026!", salt)
        assertEquals(32, hash.size)
        assertTrue(PasswordHasher.matches("RutaDemo2026!", salt, hash))
        assertFalse(PasswordHasher.matches("incorrecta", salt, hash))
        assertFalse(hash.contentEquals(PasswordHasher.hash("RutaDemo2026!", other)))
    }
}

