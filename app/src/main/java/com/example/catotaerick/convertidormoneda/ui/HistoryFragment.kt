package com.example.catotaerick.convertidormoneda.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.catotaerick.convertidormoneda.AddConversionFragment
import com.example.catotaerick.convertidormoneda.R
import com.example.catotaerick.convertidormoneda.adapter.HistoryAdapter
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.example.catotaerick.convertidormoneda.viewmodel.HistoryViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class HistoryFragment : Fragment() {

    // Usamos activityViewModels para compartir el mismo ciclo de vida con la actividad principal
    private val viewModel: HistoryViewModel by activityViewModels()

    private lateinit var rvHistory: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflamos el XML usando R.layout nativo
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Enlazamos el RecyclerView a la antigua
        rvHistory = view.findViewById(R.id.rvHistory)

        // 2. Configuramos el Adapter con los clics (Misma lógica de tu vieja Activity)
        adapter = HistoryAdapter(
            onItemClick = { record ->
                // EDITAR: Selecciona el registro y abre el BottomSheet
                viewModel.selectRecord(record)
                val addFragment = AddConversionFragment()
                addFragment.show(childFragmentManager, "EditConversion")
            },
            onItemLongClick = { record ->
                // BORRADO SEGURO
                mostrarDialogoConfirmacion(record, view)
            }
        )

        // 3. Inicializar el RecyclerView de forma vertical
        rvHistory.layoutManager = LinearLayoutManager(requireContext())
        rvHistory.adapter = adapter

        // 4. Observamos cambios en el LiveData "conversions"
        viewModel.conversions.observe(viewLifecycleOwner) { listaDeConversiones ->
            adapter.submitList(listaDeConversiones)
        }
    }

    // Función para borrado seguro adaptada al Fragment
    private fun mostrarDialogoConfirmacion(record: ConversionRecord, rootView: View) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar registro?")
            .setMessage("Se borrará esta conversión de tu historial. ¿Estás seguro?")
            .setNeutralButton("Cancelar", null)
            .setPositiveButton("Eliminar") { _, _ ->
                // Borramos de tu base / Firebase
                viewModel.deleteRecord(record)

                // Mostramos opción de DESHACER usando la vista raíz del fragmento
                Snackbar.make(rootView, "Registro eliminado", Snackbar.LENGTH_LONG)
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