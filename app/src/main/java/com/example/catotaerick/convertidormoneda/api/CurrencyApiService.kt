package com.example.catotaerick.convertidormoneda.api

import com.example.catotaerick.convertidormoneda.model.ExchangeRatesResponse
import retrofit2.Response
import retrofit2.http.GET

interface CurrencyApiService {
    @GET("v1/currencies/eur.json" )
    suspend fun getLatestRates(): Response<ExchangeRatesResponse>
}
