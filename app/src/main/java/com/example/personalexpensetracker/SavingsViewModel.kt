package com.example.personalexpensetracker
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
class SavingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: SavingsRepository
    private val currentUserId: String
    init {
        val database = AppDatabase.getDatabase(application)
        val savingsDao = database.savingsDao()
        repository = SavingsRepository(savingsDao)
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    fun getTotalSavings(): LiveData<Double?> {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return repository.getTotalSavings(currentUserId).asLiveData()
    }
    fun getTotalSavingsCallback(callback: (Double) -> Unit) = viewModelScope.launch {
        val total = repository.getTotalSavings(currentUserId).first() ?: 0.0
        callback(total)
    }
    fun getTotalSavingsForMainScreen(): LiveData<Double?> {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        return repository.getTotalSavingsForMainScreen(userId).asLiveData()
    }
    fun insert(saving: Savings) = viewModelScope.launch {
        repository.insert(saving)
    }
    fun delete(saving: Savings) = viewModelScope.launch {
        repository.delete(saving)
    }
}
