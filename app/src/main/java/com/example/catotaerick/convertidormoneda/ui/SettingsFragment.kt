package com.example.catotaerick.convertidormoneda.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.example.catotaerick.convertidormoneda.R
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var itemDefaultCurrency: LinearLayout
    private lateinit var itemDecimalPlaces: LinearLayout
    private lateinit var itemRateApp: LinearLayout
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var tvCurrentCurrency: TextView
    private lateinit var tvCurrentDecimals: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enlazamos las vistas clásicas sin Binding
        itemDefaultCurrency = view.findViewById(R.id.itemDefaultCurrency)
        itemDecimalPlaces = view.findViewById(R.id.itemDecimalPlaces)
        itemRateApp = view.findViewById(R.id.itemRateApp)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        tvCurrentCurrency = view.findViewById(R.id.tvCurrentCurrency)
        tvCurrentDecimals = view.findViewById(R.id.tvCurrentDecimals)

        // Lógica del Switch para Modo Oscuro
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                Toast.makeText(requireContext(), "Modo Oscuro Activado", Toast.LENGTH_SHORT).show()
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                Toast.makeText(requireContext(), "Modo Claro Activado", Toast.LENGTH_SHORT).show()
            }
        }

        // Configuración de clics en las opciones
        itemDefaultCurrency.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar moneda base", Toast.LENGTH_SHORT).show()
            // Aquí puedes abrir un diálogo para seleccionar USD, EUR, etc.
        }

        itemDecimalPlaces.setOnClickListener {
            Toast.makeText(requireContext(), "Cambiar precisión decimal", Toast.LENGTH_SHORT).show()
        }

        itemRateApp.setOnClickListener {
            Toast.makeText(requireContext(), "¡Gracias por tu apoyo!", Toast.LENGTH_SHORT).show()
        }
    }
}