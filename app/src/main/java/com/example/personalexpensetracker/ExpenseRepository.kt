package com.example.personalexpensetracker
import androidx.lifecycle.LiveData
import com.example.personalexpensetracker.SavingsDao
import kotlinx.coroutines.flow.Flow
class ExpenseRepository(private val expenseDao: ExpenseDao, private val SavingsDao: SavingsDao) {
    val allExpenses: LiveData<List<Expense>> = expenseDao.getAllExpenses() as LiveData<List<Expense>>
    fun getAllExpenses(userId: String): Flow<List<Expense>> {
        return expenseDao.getAllExpenses(userId)
    }
    fun getExpensesForUser(userId: String): List<Expense> {
        return expenseDao.getExpensesByUser(userId)
    }
    fun getTotalExpenses(userId: String): Flow<Double> {
        return expenseDao.getTotalExpenses(userId)
    }
    fun getFixedExpenses(userId: String): Flow<List<Expense>> {
        return expenseDao.getFixedExpenses(userId)
    }
    suspend fun insert(expense: Expense) {
        expenseDao.insertExpense(expense)
    }
    suspend fun delete(expense: Expense) {
        expenseDao.deleteExpense(expense)
    }
    fun getExpensesInRange(userId: String, start: Long, end: Long) = expenseDao.getExpensesInRange(userId, start, end)
}
