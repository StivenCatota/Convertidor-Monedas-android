package com.example.catotaerick.convertidormoneda

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.catotaerick.convertidormoneda.adapter.HistoryAdapter
import com.example.catotaerick.convertidormoneda.databinding.ActivityHistoryBinding
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private val viewModel: HistoryViewModel by viewModels()

    // Cambiamos a lateinit para configurarlo en el onCreate
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. Configurar UI básica
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { onBackPressed() }

        // 2. Configurar el Adapter con las acciones de EDITAR y BORRAR
        adapter = HistoryAdapter(
            onItemClick = { record ->
                // Acción al tocar: Abrir formulario para EDITAR
                viewModel.selectRecord(record)
                val addFragment = AddConversionFragment()
                addFragment.show(supportFragmentManager, "EditConversion")
            },
            onItemLongClick = { record ->
                // Acción al mantener presionado: BORRADO SEGURO
                mostrarDialogoConfirmacion(record)
            }
        )

        // 3. Configurar el RecyclerView
        binding.rvHistory.layoutManager = LinearLayoutManager(this)
        binding.rvHistory.adapter = adapter

        // 4. Observar los cambios en la lista
        viewModel.conversions.observe(this) { listaDeConversiones ->
            adapter.submitList(listaDeConversiones)
        }
    }

    // FUNCIÓN PARA EL BORRADO SEGURO
    private fun mostrarDialogoConfirmacion(record: ConversionRecord) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar registro?")
            .setMessage("Se borrará esta conversión de tu historial. ¿Estás seguro?")
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                // Borramos de Firebase
                viewModel.deleteRecord(record)

                // Mostramos opción de DESHACER
                Snackbar.make(binding.root, "Registro eliminado", Snackbar.LENGTH_LONG)
                    .setAction("DESHACER") {
                        viewModel.saveConversion(
                            record.fromCurrency,
                            record.toCurrency,
                            record.amount,
                            record.result
                        )
                    }.show()
            }
            .show()
    }
}
