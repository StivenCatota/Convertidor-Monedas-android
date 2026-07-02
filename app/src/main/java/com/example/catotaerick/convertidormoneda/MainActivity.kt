package com.example.catotaerick.convertidormoneda

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
// Importamos correctamente tu archivo Worker apuntando a tu paquete raíz
import com.example.catotaerick.convertidormoneda.CurrencyWorker
import com.example.catotaerick.convertidormoneda.databinding.ActivityMainBinding
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel
import com.example.catotaerick.convertidormoneda.model.ApiState
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: HistoryViewModel by viewModels()

    // Tasas fijas de respaldo (Backup)
    private val exchangeRates = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "MXN" to 17.0, "JPY" to 150.0
    )
/*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- NUEVO: PEDIR PERMISOS PARA NOTIFICACIONES (Android 13+) ---
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                101
            )
        }

        // Crear canal de notificaciones por seguridad antes de lanzar tareas
        createNotificationChannel()

        // Configuración de la tarea periódica con WorkManager
        val workRequest = PeriodicWorkRequestBuilder<CurrencyWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build())
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "CurrencyUpdateWork",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )

        // Pedimos las tasas reales al iniciar
        viewModel.fetchRates()

        // Observamos el estado de la API
        viewModel.apiState.observe(this) { state ->
            when (state) {
                is ApiState.Loading -> {
                    Toast.makeText(this, "Loading: Cargando tasas...", Toast.LENGTH_SHORT).show()
                }
                is ApiState.Success -> {
                    Toast.makeText(this, "Success: Tasas cargadas correctamente", Toast.LENGTH_SHORT).show()
                }
                is ApiState.Error -> {
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        val currencies = exchangeRates.keys.toTypedArray()
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencies)
        binding.spFrom.setAdapter(adapter)
        binding.spTo.setAdapter(adapter)

        binding.btnConvert.setOnClickListener { performConversion() }
        binding.btnViewHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        binding.btnOpenAddForm.setOnClickListener {
            val addFragment = AddConversionFragment()
            addFragment.show(supportFragmentManager, "AddConversion")
        }
    }

*/
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    binding = ActivityMainBinding.inflate(layoutInflater)
    setContentView(binding.root)

    // 1. Pedir permisos para notificaciones (Android 13+)
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        androidx.core.app.ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
            101
        )
    }

    // 2. Crear canal de notificaciones (Obligatorio)
    createNotificationChannel()

    // --- CAMBIO PARA CAPTURA: Lanzar notificación DE INMEDIATO ---
    // Usamos OneTimeWorkRequestBuilder en lugar de Periodic para que salga ya mismo
    val workRequest = androidx.work.OneTimeWorkRequestBuilder<CurrencyWorker>()
        .build()

    androidx.work.WorkManager.getInstance(this).enqueue(workRequest)
    // -------------------------------------------------------------

    // 3. Lógica de la API y Tasas
    viewModel.fetchRates()

    // Observamos el estado de la API para mostrar mensajes al usuario
    viewModel.apiState.observe(this) { state ->
        when (state) {
            is com.example.catotaerick.convertidormoneda.model.ApiState.Loading -> {
                Toast.makeText(this, "Cargando tasas reales...", Toast.LENGTH_SHORT).show()
            }
            is com.example.catotaerick.convertidormoneda.model.ApiState.Success -> {
                Toast.makeText(this, "Tasas actualizadas desde internet", Toast.LENGTH_SHORT).show()
            }
            is com.example.catotaerick.convertidormoneda.model.ApiState.Error -> {
                Toast.makeText(this, "Error de red: ${state.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 4. Configurar Selectores y Botones
    val currencies = exchangeRates.keys.toTypedArray()
    val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, currencies)
    binding.spFrom.setAdapter(adapter)
    binding.spTo.setAdapter(adapter)

    binding.btnConvert.setOnClickListener { performConversion() }
    binding.btnViewHistory.setOnClickListener {
        startActivity(Intent(this, HistoryActivity::class.java))
    }
    binding.btnLogout.setOnClickListener {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
    binding.btnOpenAddForm.setOnClickListener {
        val addFragment = AddConversionFragment()
        addFragment.show(supportFragmentManager, "AddConversion")
    }
}

    private fun performConversion() {
        val amountStr = binding.etAmount.text.toString()
        val from = binding.spFrom.text.toString()
        val to = binding.spTo.text.toString()

        if (amountStr.isEmpty() || from.isEmpty() || to.isEmpty()) {
            Toast.makeText(this, "Completa los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDouble()

        // Si la API tuvo éxito, usamos esas tasas reales. Si no, usamos las fijas de respaldo.
        val currentRates = if (viewModel.apiState.value is ApiState.Success) {
            (viewModel.apiState.value as ApiState.Success).data
        } else {
            exchangeRates
        }

        val rateTo = currentRates[to.lowercase()] ?: currentRates[to.uppercase()] ?: 1.0
        val rateFrom = currentRates[from.lowercase()] ?: currentRates[from.uppercase()] ?: 1.0
        val result = amount * (rateTo / rateFrom)

        viewModel.saveConversion(from, to, amount, result)
        Toast.makeText(this, "Resultado Real: %.2f %s".format(result, to), Toast.LENGTH_LONG).show()
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
}