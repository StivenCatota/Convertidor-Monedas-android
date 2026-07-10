// util/ConversionCalculator.kt
package com.example.catotaerick.convertidormoneda.util

object ConversionCalculator {
    fun calcularResultado(amount: Double, from: String, to: String, rates: Map<String, Double>): Double {
        val rateTo = rates[to.lowercase()] ?: rates[to.uppercase()] ?: 1.0
        val rateFrom = rates[from.lowercase()] ?: rates[from.uppercase()] ?: 1.0
        return amount * (rateTo / rateFrom)
    }
}