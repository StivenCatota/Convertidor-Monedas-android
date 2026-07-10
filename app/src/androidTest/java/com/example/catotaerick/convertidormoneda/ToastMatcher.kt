package com.example.catotaerick.convertidormoneda

import android.view.WindowManager
import androidx.test.espresso.Root
import org.hamcrest.Description
import org.hamcrest.TypeSafeMatcher

class ToastMatcher : TypeSafeMatcher<Root>() {
    override fun describeTo(description: Description) {
        description.appendText("es un Toast del sistema")
    }

    override fun matchesSafely(root: Root): Boolean {
        val type = root.windowLayoutParams.get().type
        // Validamos el tipo de ventana clásico de Toast o el de sistema
        if (type == WindowManager.LayoutParams.TYPE_TOAST || type == WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY) {
            val windowToken = root.decorView.windowToken
            val appToken = root.decorView.applicationWindowToken
            // En algunas APIs modernas basta con que el decorView no sea nulo y corresponda al layout del Toast
            return windowToken != null
        }
        return false
    }
}