package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Prescription
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionDao {
    @Query("SELECT * FROM prescriptions ORDER BY issueDateMillis DESC")
    fun getAllPrescriptions(): Flow<List<Prescription>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prescription: Prescription): Long

    @Update
    suspend fun update(prescription: Prescription)

    @Delete
    suspend fun delete(prescription: Prescription)

    @Query("DELETE FROM prescriptions WHERE id = :id")
    suspend fun deleteById(id: Long)
}
