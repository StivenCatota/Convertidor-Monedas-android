package com.example.catotaerick.convertidormoneda

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.example.catotaerick.convertidormoneda.databinding.LayoutAddConversionBinding
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AddConversionFragment : BottomSheetDialogFragment() {

    private var _binding: LayoutAddConversionBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HistoryViewModel by activityViewModels()

    private var recordToEdit: ConversionRecord? = null

    // Estas son tus tasas fijas (se usarán si no hay internet)
    private val exchangeRates = mapOf(
        "USD" to 1.0, "EUR" to 0.92, "GBP" to 0.79, "MXN" to 17.0, "JPY" to 150.0
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = LayoutAddConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- NUEVO: Pedimos los precios reales al abrir el formulario ---
        viewModel.fetchRates()

        // Configurar los selectores (Spinners)
        val currencies = exchangeRates.keys.toTypedArray()
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, currencies)
        binding.spFrom.setAdapter(adapter)
        binding.spTo.setAdapter(adapter)

        // Lógica de edición: Rellenar campos si estamos editando
        viewModel.selectedRecord.observe(viewLifecycleOwner) { record ->
            if (record != null) {
                recordToEdit = record
                binding.etAmount.setText(record.amount.toString())
                binding.spFrom.setText(record.fromCurrency, false)
                binding.spTo.setText(record.toCurrency, false)
                binding.btnSave.text = "ACTUALIZAR REGISTRO"
            }
        }

        binding.btnSave.setOnClickListener {
            val amountStr = binding.etAmount.text.toString()
            val from = binding.spFrom.text.toString()
            val to = binding.spTo.text.toString()

            if (amountStr.isNotEmpty() && from.isNotEmpty() && to.isNotEmpty()) {
                val amount = amountStr.toDouble()

                // --- ACTUALIZACIÓN: Lógica de conversión inteligente ---
                // Intentamos usar las tasas de internet (realRates), si no, usamos las fijas
                val rates = viewModel.realRates.value ?: exchangeRates

                // Calculamos el resultado usando las tasas (convertimos a minúsculas para la API)
                val rateTo = rates[to.uppercase()] ?: rates[to.lowercase()] ?: 1.0
                val rateFrom = rates[from.uppercase()] ?: rates[from.lowercase()] ?: 1.0
                val result = amount * (rateTo / rateFrom)

                if (recordToEdit != null) {
                    // MODO EDICIÓN
                    val updatedRecord = recordToEdit!!.copy(
                        fromCurrency = from,
                        toCurrency = to,
                        amount = amount,
                        result = result
                    )
                    viewModel.updateRecord(updatedRecord)
                    Toast.makeText(context, "Actualizado correctamente", Toast.LENGTH_SHORT).show()
                } else {
                    // MODO CREACIÓN
                    viewModel.saveConversion(from, to, amount, result)
                    Toast.makeText(context, "Guardado exitosamente", Toast.LENGTH_SHORT).show()
                }

                dismiss()
            } else {
                Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.selectRecord(null)
        _binding = null
    }
}
