package com.example.catotaerick.convertidormoneda.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.catotaerick.convertidormoneda.databinding.ItemConversionBinding
import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import java.text.SimpleDateFormat
import java.util.*

// ACTUALIZACIÓN: Ahora el adapter recibe las acciones de clic y clic largo al crearse
class HistoryAdapter(
    private val onItemClick: (ConversionRecord) -> Unit,
    private val onItemLongClick: (ConversionRecord) -> Unit
) : ListAdapter<ConversionRecord, HistoryAdapter.ViewHolder>(DiffCallback) {

    class ViewHolder(private val binding: ItemConversionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: ConversionRecord,
            clickListener: (ConversionRecord) -> Unit,
            longClickListener: (ConversionRecord) -> Unit
        ) {
            // CORRECCIÓN: Usamos tvConversionText que es el ID real de tu XML
            binding.tvConversionText.text = "${item.amount} ${item.fromCurrency} -> ${String.format("%.2f", item.result)} ${item.toCurrency}"

            val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            binding.tvTimestamp.text = sdf.format(Date(item.timestamp))

            // CLIC SIMPLE: Para editar
            binding.root.setOnClickListener { clickListener(item) }

            // CLIC LARGO: Para borrar
            binding.root.setOnLongClickListener {
                longClickListener(item)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemConversionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        // Pasamos las acciones al ViewHolder
        holder.bind(item, onItemClick, onItemLongClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ConversionRecord>() {
        override fun areItemsTheSame(oldItem: ConversionRecord, newItem: ConversionRecord): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: ConversionRecord, newItem: ConversionRecord): Boolean {
            return oldItem == newItem
        }
    }
}
