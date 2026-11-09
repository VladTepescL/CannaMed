package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DosageLog
import kotlinx.coroutines.flow.Flow

@Dao
interface DosageLogDao {
    @Query("SELECT * FROM dosage_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<DosageLog>>

    @Query("SELECT * FROM dosage_logs WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getLogsBetween(startTime: Long, endTime: Long): Flow<List<DosageLog>>

    @Query("SELECT * FROM dosage_logs WHERE cartridgeId = :cartridgeId ORDER BY timestamp DESC")
    fun getLogsForCartridge(cartridgeId: Long): Flow<List<DosageLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DosageLog): Long

    @Update
    suspend fun update(log: DosageLog)

    @Delete
    suspend fun delete(log: DosageLog)

    @Query("DELETE FROM dosage_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
