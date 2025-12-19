package com.example.personalexpensetracker
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
@Dao
interface SavingsDao {
    @Query("SELECT * FROM savings WHERE userId = :userId ORDER BY date DESC")
    fun getAllSavings(userId: String): Flow<List<Savings>>
    @Query("SELECT SUM(amount) FROM savings WHERE userId = :userId")
    fun getTotalSavings(userId: String): Flow<Double>
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertSaving(savings: Savings)
    @Delete
    suspend fun delete(savings: Savings)
    @Query("SELECT * FROM savings ORDER BY id DESC")
    fun getAllSavings(): LiveData<List<Savings>>
    @Insert
    suspend fun insert(saving: Savings)
}