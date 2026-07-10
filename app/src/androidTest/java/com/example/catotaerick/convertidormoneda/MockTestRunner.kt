package com.example.catotaerick.convertidormoneda

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.google.firebase.database.FirebaseDatabase

class MockTestRunner : AndroidJUnitRunner() {
    override fun newApplication(cl: ClassLoader?, className: String?, context: Context?): Application {
        // CONFIGURAMOS EL EMULADOR AQUÍ, ANTES DE QUE LA APP SE INICIE
        try {
            FirebaseDatabase.getInstance().useEmulator("10.0.2.2", 9000)
        } catch (e: Exception) { }

        return super.newApplication(cl, className, context)
    }
}
