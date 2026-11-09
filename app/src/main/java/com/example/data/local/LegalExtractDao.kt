package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.LegalExtract
import kotlinx.coroutines.flow.Flow

@Dao
interface LegalExtractDao {
    @Query("SELECT * FROM legal_extracts ORDER BY id ASC")
    fun getAllLegalExtracts(): Flow<List<LegalExtract>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(extract: LegalExtract): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(extracts: List<LegalExtract>)

    @Update
    suspend fun update(extract: LegalExtract)

    @Delete
    suspend fun delete(extract: LegalExtract)

    @Query("SELECT COUNT(*) FROM legal_extracts")
    suspend fun count(): Int
}
