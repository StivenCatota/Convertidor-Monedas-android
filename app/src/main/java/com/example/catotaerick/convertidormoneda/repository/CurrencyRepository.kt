package com.example.catotaerick.convertidormoneda.repository

import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.google.firebase.database.*

// ACTUALIZACIÓN: Ahora recibe la instancia por constructor (Inyección de dependencias)
class CurrencyRepository(
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
) {
    private val database: DatabaseReference = firebaseDatabase.getReference("conversions")

    // CREATE (Insert)
    fun saveConversion(record: ConversionRecord, callback: (Boolean) -> Unit) {
        val id = database.push().key ?: return
        val recordWithId = record.copy(id = id)
        database.child(id).setValue(recordWithId)
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }

    // READ (Optimizado para Tests y lecturas únicas con addListenerForSingleValueEvent)
    fun getConversions(userId: String, onDataChanged: (List<ConversionRecord>) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addListenerForSingleValueEvent(object : ValueEventListener { // <-- CAMBIO AQUÍ para evitar lecturas vacías inmediatas
                override fun onDataChange(snapshot: DataSnapshot) {
                    val list = mutableListOf<ConversionRecord>()
                    for (child in snapshot.children) {
                        val record = child.getValue(ConversionRecord::class.java)
                        record?.let { list.add(it) }
                    }
                    onDataChanged(list.sortedByDescending { it.timestamp })
                }
                override fun onCancelled(error: DatabaseError) {}
            })
    }

    // UPDATE
    fun updateConversion(record: ConversionRecord, callback: (Boolean) -> Unit) {
        val id = record.id ?: return
        database.child(id).setValue(record)
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }

    // DELETE
    fun deleteConversion(recordId: String, callback: (Boolean) -> Unit) {
        database.child(recordId).removeValue()
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }
}