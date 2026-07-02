package com.example.catotaerick.convertidormoneda.viewmodel

import androidx.lifecycle.*
import com.example.catotaerick.convertidormoneda.api.RetrofitClient
import com.example.catotaerick.convertidormoneda.model.ApiState
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.example.catotaerick.convertidormoneda.repository.CurrencyRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class HistoryViewModel : ViewModel() {
    private val repository = CurrencyRepository()
    private val auth = FirebaseAuth.getInstance()

    // --- 1. ESTADO DE LA API (Sección 5A) ---
    private val _apiState = MutableLiveData<ApiState>()
    val apiState: LiveData<ApiState> get() = _apiState

    // Tasas de cambio reales obtenidas desde la API
    private val _realRates = MutableLiveData<Map<String, Double>>()
    val realRates: LiveData<Map<String, Double>> get() = _realRates


    // --- 2. ESTADO DEL HISTORIAL (Firebase) ---
    private val _conversions = MutableLiveData<List<ConversionRecord>>()
    val conversions: LiveData<List<ConversionRecord>> get() = _conversions

    // --- 3. ESTADO DE EDICIÓN ---
    private val _selectedRecord = MutableLiveData<ConversionRecord?>()
    val selectedRecord: LiveData<ConversionRecord?> get() = _selectedRecord

    init {
        loadHistory()
        fetchRates() // Pedimos las tasas al iniciar
    }

    fun selectRecord(record: ConversionRecord?) {
        _selectedRecord.value = record
    }

    // FUNCIÓN PARA LA API
    fun fetchRates() {

        _apiState.value = ApiState.Loading

        viewModelScope.launch {

            try {

                val response = RetrofitClient.apiService.getLatestRates()
                if (response.isSuccessful && response.body() != null) {

                    val rates = response.body()!!.rates

                    // Guardar las tasas para que AddConversionFragment pueda usarlas
                    _realRates.value = rates

                    // Actualizar el estado de la API
                    _apiState.value = ApiState.Success(rates)

                } else {

                    _apiState.value = ApiState.Error("\"Código: \${response.code()} - \${response.message()}")

                }

            } catch (e: Exception) {

                _apiState.value = ApiState.Error(
                    e.message ?: "Error desconocido"
                )

            }

        }

    }

    // FUNCIÓN PARA CARGAR DE FIREBASE
    fun loadHistory() {
        val userId = auth.currentUser?.uid ?: return
        repository.getConversions(userId) { list ->
            _conversions.value = list
        }
    }

    // ESTA ES LA FUNCIÓN QUE TE DABA ERROR
    fun saveConversion(from: String, to: String, amount: Double, result: Double) {
        val userId = auth.currentUser?.uid ?: return
        val record = ConversionRecord(
            fromCurrency = from,
            toCurrency = to,
            amount = amount,
            result = result,
            userId = userId,
            timestamp = System.currentTimeMillis()
        )
        viewModelScope.launch {
            repository.saveConversion(record) { /* éxito */ }
        }
    }

    fun deleteRecord(record: ConversionRecord) {
        record.id?.let { id ->
            viewModelScope.launch {
                repository.deleteConversion(id) { /* éxito */ }
            }
        }
    }

    fun updateRecord(record: ConversionRecord) {
        viewModelScope.launch {
            repository.updateConversion(record) { /* éxito */ }
        }
    }
}
