package com.example.catotaerick.convertidormoneda

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.example.catotaerick.convertidormoneda.repository.CurrencyRepository
import com.google.firebase.database.FirebaseDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class CurrencyRepositoryTest {

    private lateinit var repository: CurrencyRepository
    private lateinit var database: FirebaseDatabase

    @Before
    fun setup() {
        database = FirebaseDatabase.getInstance()
        try {
            database.useEmulator("10.0.2.2", 9000)
        } catch (e: IllegalStateException) {
            // Ignorar si ya está configurado
        }

        // SOLUCIÓN: Le pasamos la instancia segura del emulador al repositorio
        repository = CurrencyRepository(database)
    }

    @After
    fun tearDown() {
        // LIMPIEZA: Es vital borrar los datos después de cada prueba
        // para que la siguiente prueba empiece desde cero (como Room en memoria).
        val latch = CountDownLatch(1)
        database.getReference("conversions").removeValue().addOnCompleteListener {
            latch.countDown()
        }
        latch.await(2, TimeUnit.SECONDS)
    }

    @Test
    fun testSaveAndReadConversion() {
        val latch = CountDownLatch(1)
        val record = ConversionRecord(
            fromCurrency = "USD", toCurrency = "EUR",
            amount = 100.0, result = 92.0, userId = "user_test_123",
            timestamp = System.currentTimeMillis()
        )

        // Prueba de Inserción
        repository.saveConversion(record) { success ->
            assertTrue("La inserción falló", success)

            // Prueba de Lectura
            repository.getConversions("user_test_123") { list ->
                assertEquals("Debería haber exactamente 1 registro", 1, list.size)
                assertEquals("USD", list[0].fromCurrency)
                assertEquals(92.0, list[0].result, 0.01)
                latch.countDown()
            }
        }

        assertTrue("La prueba tardó demasiado", latch.await(10, TimeUnit.SECONDS))
    }

    @Test
    fun testFilterByUserId() {
        val latch = CountDownLatch(1)
        val r1 = ConversionRecord(fromCurrency = "A", toCurrency = "B", amount = 1.0, result = 1.0, userId = "profe_user")
        val r2 = ConversionRecord(fromCurrency = "C", toCurrency = "D", amount = 2.0, result = 2.0, userId = "otro_user")

        // Guardamos dos registros de usuarios diferentes
        repository.saveConversion(r1) {
            repository.saveConversion(r2) {
                // Verificamos que solo recupere los del usuario solicitado
                repository.getConversions("profe_user") { list ->
                    assertEquals("El filtro falló: debería haber 1 registro", 1, list.size)
                    assertEquals("El userId no coincide", "profe_user", list[0].userId)
                    latch.countDown()
                }
            }
        }

        assertTrue("La prueba de filtrado tardó demasiado", latch.await(10, TimeUnit.SECONDS))
    }
}
