package com.example.catotaerick.convertidormoneda.model

data class ConversionRecord(
    val id: String? = null,
    val fromCurrency: String = "",
    val toCurrency: String = "",
    val amount: Double = 0.0,
    val result: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String = ""
)
