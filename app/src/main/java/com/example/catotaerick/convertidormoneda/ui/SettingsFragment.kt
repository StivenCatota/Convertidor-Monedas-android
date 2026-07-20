package com.example.catotaerick.convertidormoneda.ui

import android.content.Context
import android.content.SharedPreferences
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
import android.content.Intent
import com.example.catotaerick.convertidormoneda.LoginActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth

class SettingsFragment : Fragment() {

    private lateinit var itemDefaultCurrency: LinearLayout
    private lateinit var itemDecimalPlaces: LinearLayout
    private lateinit var itemRateApp: LinearLayout
    private lateinit var itemLogout: LinearLayout
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var tvCurrentCurrency: TextView
    private lateinit var tvCurrentDecimals: TextView

    companion object {
        private const val PREFS_NAME = "app_preferences"
        private const val KEY_DARK_MODE = "dark_mode_enabled"

        /**
         * Aplica el modo de tema guardado en SharedPreferences.
         * Debe llamarse antes de setContentView() en cada Activity.
         */
        fun applyTheme(context: Context) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val mode = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            AppCompatDelegate.setDefaultNightMode(mode)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Enlazamos las vistas
        itemDefaultCurrency = view.findViewById(R.id.itemDefaultCurrency)
        itemDecimalPlaces = view.findViewById(R.id.itemDecimalPlaces)
        itemRateApp = view.findViewById(R.id.itemRateApp)
        itemLogout = view.findViewById(R.id.itemLogout)
        switchDarkMode = view.findViewById(R.id.switchDarkMode)
        tvCurrentCurrency = view.findViewById(R.id.tvCurrentCurrency)
        tvCurrentDecimals = view.findViewById(R.id.tvCurrentDecimals)

        // ── Sincronizar el switch con el estado real del tema ──
        val prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val currentMode = prefs.getInt(KEY_DARK_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        when (currentMode) {
            AppCompatDelegate.MODE_NIGHT_YES -> {
                switchDarkMode.isChecked = true
            }
            AppCompatDelegate.MODE_NIGHT_NO -> {
                switchDarkMode.isChecked = false
            }
            else -> {
                // MODE_NIGHT_FOLLOW_SYSTEM: el switch refleja lo que el sistema está usando ahora
                val isNight = (resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK) ==
                        android.content.res.Configuration.UI_MODE_NIGHT_YES
                switchDarkMode.isChecked = isNight
            }
        }

        // ── Lógica del Switch para Modo Oscuro ──
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            val mode = if (isChecked) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }

            // Guardar la preferencia
            prefs.edit().putInt(KEY_DARK_MODE, mode).apply()

            // Aplicar el modo
            AppCompatDelegate.setDefaultNightMode(mode)

            val mensaje = if (isChecked) "Modo Oscuro Activado" else "Modo Claro Activado"
            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_SHORT).show()
        }

        // Configuración de clics en las opciones
//        itemDefaultCurrency.setOnClickListener {
//            Toast.makeText(requireContext(), "Cambiar moneda base", Toast.LENGTH_SHORT).show()
//        }
//
//        itemDecimalPlaces.setOnClickListener {
//            Toast.makeText(requireContext(), "Cambiar precisión decimal", Toast.LENGTH_SHORT).show()
//        }
//
//        itemRateApp.setOnClickListener {
//            Toast.makeText(requireContext(), "¡Gracias por tu apoyo!", Toast.LENGTH_SHORT).show()
//        }
        itemLogout.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("¿Cerrar sesión?")
                .setMessage("Vas a salir de tu cuenta.")
                .setNeutralButton("Cancelar", null)
                .setPositiveButton("Cerrar sesión") { _, _ ->
                    FirebaseAuth.getInstance().signOut()
                    val intent = Intent(requireActivity(), LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    requireActivity().finish()
                }
                .show()
        }
    }
}
