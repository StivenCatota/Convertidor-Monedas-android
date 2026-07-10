package com.example.catotaerick.convertidormoneda

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LoginUITest {

    @get:Rule
    val activityRule = ActivityScenarioRule(LoginActivity::class.java)

    @Test
    fun loginConCamposVacios_muestraMensajeError() {
        // 1. Simula el clic en el botón de login
        onView(withId(R.id.btnLogin)).perform(click())

        // 2. Al ser un Snackbar, Espresso lo encuentra de golpe en la misma pantalla sin cambiar de Root
        onView(withText("Completa los campos")).check(matches(isDisplayed()))
    }
}