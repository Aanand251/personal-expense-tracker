package com.example.personalexpensetracker

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("SELECT * FROM expense ORDER BY id DESC")
    fun getAllExpensesLegacy(): LiveData<List<Expense>>

    @Query("SELECT * FROM expense WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpenses(userId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expense WHERE userId = :userId AND isFixed = 1 ORDER BY date DESC")
    fun getFixedExpenses(userId: String): Flow<List<Expense>>

    @Query("SELECT SUM(amount) FROM expense WHERE userId = :userId")
    fun getTotalExpenses(userId: String): Flow<Double>

    @Query("SELECT * FROM expense ORDER BY id DESC")
    fun getAllExpenses(): LiveData<List<Expense>>

    @Query("SELECT * FROM expense WHERE userId = :userId AND date BETWEEN :start AND :end ORDER BY date DESC")
    fun getExpensesInRange(userId: String, start: Long, end: Long): Flow<List<Expense>>

//    @Query("SELECT category, SUM(amount) as amount FROM expense WHERE userId = :userId GROUP BY category")
//    fun getCategoryWiseExpense(userId: String): List<Expense>

    @Query("SELECT * FROM expense WHERE userId = :userId")
    fun getExpensesByUser(userId: String): List<Expense>

}
