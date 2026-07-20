package com.example.catotaerick.convertidormoneda.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView // O AutoCompleteTextView / EditText dependiendo de tu XML
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.catotaerick.convertidormoneda.R
import com.example.catotaerick.convertidormoneda.model.ApiState
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel


class ConvertFragment : Fragment() {

    private val viewModel: HistoryViewModel by activityViewModels()

    // Declaramos las vistas clásicas sin Binding
    private lateinit var etAmount: EditText
    private lateinit var etResult: EditText
    private lateinit var spFrom: AutoCompleteTextView
    private lateinit var spTo: AutoCompleteTextView
    private lateinit var btnConvert: Button
    private lateinit var btnSwapCurrencies: Button

    private val fallbackExchangeRates = mutableMapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "JPY" to 156.71,
        "CAD" to 1.37, "AUD" to 1.50, "MXN" to 17.0
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        return inflater.inflate(R.layout.fragment_convert, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enlazamos las vistas a la antigüita con findViewByID
        etAmount = view.findViewById(R.id.etAmount)
        etResult = view.findViewById(R.id.etResult)
        spFrom = view.findViewById(R.id.spFrom)
        spTo = view.findViewById(R.id.spTo)
        btnConvert = view.findViewById(R.id.btnConvert)
        btnSwapCurrencies = view.findViewById(R.id.btnSwapCurrencies)

        viewModel.apiState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is ApiState.Loading -> {
                    Toast.makeText(requireContext(), "Cargando tasas...", Toast.LENGTH_SHORT).show()
                }
                is ApiState.Success -> {
                    setupCurrencySpinners(state.data.keys.toList().map { it.uppercase() })
                }
                is ApiState.Error -> {
                    Toast.makeText(requireContext(), "Error de red. Usando respaldo.", Toast.LENGTH_LONG).show()
                    setupCurrencySpinners(fallbackExchangeRates.keys.toList())
                }
            }
        }

        btnConvert.setOnClickListener { performConversion() }
        btnSwapCurrencies.setOnClickListener { swapCurrencies() }


    }

    private fun setupCurrencySpinners(currencies: List<String>) {
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        spFrom.setAdapter(adapter)
        spTo.setAdapter(adapter)

        if (spFrom.text.isEmpty()) spFrom.setText("USD", false)
        if (spTo.text.isEmpty()) spTo.setText("EUR", false)
    }

    private fun performConversion() {
        val amountStr = etAmount.text.toString()
        val from = spFrom.text.toString()
        val to = spTo.text.toString()

        if (amountStr.isEmpty() || from.isEmpty() || to.isEmpty()) {
            Toast.makeText(requireContext(), "Completa los campos", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(requireContext(), "Ingresa un número válido", Toast.LENGTH_SHORT).show()
            return
        }

        val currentRates = if (viewModel.apiState.value is ApiState.Success) {
            (viewModel.apiState.value as ApiState.Success).data
        } else {
            fallbackExchangeRates
        }

        val rateTo = currentRates[to.lowercase()] ?: currentRates[to.uppercase()] ?: 1.0
        val rateFrom = currentRates[from.lowercase()] ?: currentRates[from.uppercase()] ?: 1.0
        val result = amount * (rateTo / rateFrom)

        etResult.setText(String.format("%.2f", result))
        viewModel.saveConversion(from, to, amount, result)
    }

    private fun swapCurrencies() {
        val from = spFrom.text.toString()
        val to = spTo.text.toString()
        spFrom.setText(to, false)
        spTo.setText(from, false)
    }
}