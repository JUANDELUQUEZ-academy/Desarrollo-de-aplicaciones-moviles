package com.example.myapplication

import android.content.Context
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
            onView(withId(R.id.btnLogin)).check(matches(isDisplayed()))
            assertNull(Session.userId)
        }
    }
}
