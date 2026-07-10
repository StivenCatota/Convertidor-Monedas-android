package com.example.catotaerick.convertidormoneda.util

import org.junit.Assert.assertEquals
import org.junit.Test

class ConversionCalculatorTest {

    private val ratesDePrueba = mapOf(
        "usd" to 1.0,
        "eur" to 0.92,
        "gbp" to 0.79
    )

    @Test
    fun calcularResultado_usdAEur_retornaResultadoCorrecto() {
        // ARRANGE
        val amount = 100.0
        val from = "USD"
        val to = "EUR"

        // ACT
        val resultado = ConversionCalculator.calcularResultado(amount, from, to, ratesDePrueba)

        // ASSERT
        assertEquals("100 USD a EUR debe dar 92.0", 92.0, resultado, 0.001)
    }

    @Test
    fun calcularResultado_monedaNoExisteEnMapa_usaTasaPorDefectoUno() {
        // ARRANGE
        val amount = 50.0
        val from = "USD"
        val to = "XYZ" // moneda inexistente en el mapa

        // ACT
        val resultado = ConversionCalculator.calcularResultado(amount, from, to, ratesDePrueba)

        // ASSERT
        assertEquals("Si la moneda destino no existe, debe usar 1.0 como tasa por defecto", 50.0, resultado, 0.001)
    }

    @Test
    fun calcularResultado_montoCero_retornaCero() {
        // ARRANGE
        val amount = 0.0
        val from = "USD"
        val to = "EUR"

        // ACT
        val resultado = ConversionCalculator.calcularResultado(amount, from, to, ratesDePrueba)

        // ASSERT
        assertEquals("Convertir un monto de 0 siempre debe dar 0, sin importar la tasa", 0.0, resultado, 0.001)
    }

    @Test
    fun calcularResultado_codigoEnMinusculas_seNormalizaCorrectamente() {
        // ARRANGE
        val amount = 10.0
        val from = "usd" // minúsculas, distinto al mapa que usa "usd" también pero probamos mezcla
        val to = "EUR"   // mayúsculas

        // ACT
        val resultado = ConversionCalculator.calcularResultado(amount, from, to, ratesDePrueba)

        // ASSERT
        assertEquals("La función debe normalizar mayúsculas/minúsculas y encontrar la tasa igual", 9.2, resultado, 0.001)
    }
}