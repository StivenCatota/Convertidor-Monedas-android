package com.example.catotaerick.convertidormoneda.model

import com.google.gson.annotations.SerializedName

data class ExchangeRatesResponse(
    @SerializedName("date") val date: String,
    @SerializedName("eur") val rates: Map<String, Double>
)

// Clase sellada para manejar los estados de la API (Loading, Success, Error)
sealed class ApiState {
    object Loading : ApiState()
    data class Success(val data: Map<String, Double>) : ApiState()
    data class Error(val message: String) : ApiState()
}
