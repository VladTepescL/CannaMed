package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Cartridge
import kotlinx.coroutines.flow.Flow

@Dao
interface CartridgeDao {
    @Query("SELECT * FROM cartridges ORDER BY installedTimestamp DESC")
    fun getAllCartridges(): Flow<List<Cartridge>>

    @Query("SELECT * FROM cartridges WHERE isActive = 1 ORDER BY installedTimestamp DESC")
    fun getActiveCartridges(): Flow<List<Cartridge>>

    @Query("SELECT * FROM cartridges WHERE id = :id")
    fun getCartridgeById(id: Long): Flow<Cartridge?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(cartridge: Cartridge): Long

    @Update
    suspend fun update(cartridge: Cartridge)

    @Delete
    suspend fun delete(cartridge: Cartridge)

    @Query("UPDATE cartridges SET totalPuffsLogged = totalPuffsLogged + :puffs WHERE id = :id")
    suspend fun addPuffs(id: Long, puffs: Int)

    @Query("UPDATE cartridges SET isActive = 0, finishedTimestamp = :finishedTime WHERE id = :id")
    suspend fun markFinished(id: Long, finishedTime: Long)
}
