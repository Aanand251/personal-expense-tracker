package com.example.personalexpensetracker
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale
class ExpenseViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ExpenseRepository
    private val context = getApplication<Application>().applicationContext
    init {
        val db = AppDatabase.getDatabase(application)
        repository = ExpenseRepository(db.expenseDao(), db.savingsDao())
    }
    private val _expensesLiveData = MutableLiveData<List<Expense>>()
    val expensesLiveData: LiveData<List<Expense>> get() = _expensesLiveData
    fun loadUserExpenses(userId: String) {
        val expenses = repository.getExpensesForUser(userId)
        _expensesLiveData.value = expenses
    }
    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    fun getAllExpensesForUser(): LiveData<List<Expense>> {
        return repository.getAllExpenses(getCurrentUserId()).asLiveData()
    }
    fun getFixedExpensesForUser(): LiveData<List<Expense>> {
        return repository.getFixedExpenses(getCurrentUserId()).asLiveData()
    }
    fun getTotalExpensesForUser(callback: (Double) -> Unit) {
        viewModelScope.launch {
            val total = repository.getTotalExpenses(getCurrentUserId()).first() ?: 0.0
            callback(total)
        }
    }
    fun insert(expense: Expense) = viewModelScope.launch {
        repository.insert(expense)
    }
    fun delete(expense: Expense) = viewModelScope.launch {
        repository.delete(expense)
    }
    fun getExpensesInRange(userId: String, start: Long, end: Long): LiveData<List<Expense>> {
        return repository.getExpensesInRange(userId, start, end).asLiveData()
    }
}