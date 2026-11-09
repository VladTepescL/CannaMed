package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.StrainInfo
import kotlinx.coroutines.flow.Flow

@Dao
interface StrainDao {
    @Query("SELECT * FROM strains ORDER BY name ASC")
    fun getAllStrains(): Flow<List<StrainInfo>>

    @Query("SELECT * FROM strains WHERE name LIKE '%' || :query || '%' OR medicalBenefits LIKE '%' || :query || '%' OR dominantTerpenes LIKE '%' || :query || '%' ORDER BY name ASC")
    fun searchStrains(query: String): Flow<List<StrainInfo>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(strain: StrainInfo): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(strains: List<StrainInfo>)

    @Query("SELECT COUNT(*) FROM strains")
    suspend fun count(): Int
}
