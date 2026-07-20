package com.example.catotaerick.convertidormoneda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.catotaerick.convertidormoneda.databinding.ActivityMainBinding
import com.example.catotaerick.convertidormoneda.model.ApiState
import com.example.catotaerick.convertidormoneda.ui.ConvertFragment
import com.example.catotaerick.convertidormoneda.ui.HistoryFragment
import com.example.catotaerick.convertidormoneda.ui.SettingsFragment
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: HistoryViewModel

    // Tasas de cambio de respaldo en caso de que la API falle
    private val exchangeRates = mutableMapOf(
        "usd" to 1.0,
        "eur" to 0.92,
        "gbp" to 0.79,
        "jpy" to 156.71,
        "cad" to 1.37,
        "aud" to 1.50,
        "chf" to 0.90,
        "cny" to 7.25,
        "sek" to 10.68,
        "nzd" to 1.63
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ── IMPORTANTE: Aplicar el tema guardado ANTES de setContentView ──
        SettingsFragment.applyTheme(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        // Pedir permisos para notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }
        createNotificationChannel()
        scheduleDailyNotification()


        if (savedInstanceState == null) {
            loadFragment(ConvertFragment())
        }

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_convert -> {
                    loadFragment(ConvertFragment())
                    true
                }
                R.id.navigation_history -> {
                    loadFragment(HistoryFragment())
                    true
                }
                R.id.navigation_settings -> {
                    loadFragment(SettingsFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Conversión"
            val descriptionText = "Notificaciones para revisar tasas de cambio"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("CURRENCY_NOTIF", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleDailyNotification() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<CurrencyWorker>(
            24, TimeUnit.HOURS // Repetir cada 24 horas
        )
            .setInitialDelay(10, TimeUnit.SECONDS) // Iniciar 10 segundos después de la instalación
            .build()

        androidx.work.WorkManager.getInstance(this).enqueue(dailyWorkRequest)
    }

    // La lógica de conversión y los botones antiguos se moverán al ConvertFragment
    // o se adaptarán para interactuar con el ViewModel desde los fragmentos.
}
