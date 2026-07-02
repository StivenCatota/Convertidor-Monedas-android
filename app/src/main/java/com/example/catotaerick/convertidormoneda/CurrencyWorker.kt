package com.example.catotaerick.convertidormoneda

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.annotation.SuppressLint

class CurrencyWorker(
    context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @SuppressLint("MissingPermission")
    override fun doWork(): Result {
        // 1. Crear la notificación
        val builder = NotificationCompat.Builder(applicationContext, "CURRENCY_NOTIF")
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono nativo por defecto
            .setContentTitle("Tasas de Cambio")
            .setContentText("Las tasas de cambio han sido actualizadas de forma periódica.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        // 2. Lanzar la notificación de fondo
        try {
            val notificationManager = NotificationManagerCompat.from(applicationContext)
            notificationManager.notify(1, builder.build())
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Indicar que la tarea fue exitosa
        return Result.success()
    }
}