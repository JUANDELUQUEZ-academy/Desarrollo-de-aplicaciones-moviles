package com.example.myapplication

import android.content.Context
import android.os.SystemClock
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionGuardTest {
    @Test fun trackingWithoutSessionReturnsToLogin() {
        Session.end()
        ActivityScenario.launch(TrackingActivity::class.java).use {
            val deadline = SystemClock.elapsedRealtime() + 10000
            // Room consulta el rol de forma asíncrona antes de redirigir.
            var redirected = false
            while (!redirected && SystemClock.elapsedRealtime() < deadline) {
                try {
                    onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
                    redirected = true
                } catch (_: androidx.test.espresso.NoMatchingViewException) { SystemClock.sleep(100) }
            }
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
            assertNull(Session.userId)
        }
    }
}
