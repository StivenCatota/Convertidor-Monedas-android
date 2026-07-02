package com.example.catotaerick.convertidormoneda.repository

import com.example.catotaerick.convertidormoneda.model.ConversionRecord
import com.google.firebase.database.*

class CurrencyRepository {
    private val database: DatabaseReference = FirebaseDatabase.getInstance("https://convertidormoneda-34267-default-rtdb.firebaseio.com").getReference("conversions")

    // CREATE (Insert)
    fun saveConversion(record: ConversionRecord, callback: (Boolean) -> Unit) {
        val id = database.push().key ?: return
        val recordWithId = record.copy(id = id)
        database.child(id).setValue(recordWithId)
            .addOnCompleteListener { task -> callback(task.isSuccessful) }
    }

    // READ (GetAll - Tiempo real)
    fun getConversions(userId: String, onDataChanged: (List<ConversionRecord>) -> Unit) {
        database.orderByChild("userId").equalTo(userId)
            .addValueEventListener(object : ValueEventListener {
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
