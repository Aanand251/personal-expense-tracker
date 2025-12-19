package com.example.personalexpensetracker
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
class LoanViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LoanRepository
    init {
        val loanDao = AppDatabase.getDatabase(application).loanDao()
        repository = LoanRepository(loanDao)
    }
    fun getCurrentUserId(): String {
        return FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
    fun getAllLoans(): LiveData<List<Loan>> {
        return repository.getAllLoans(getCurrentUserId()).asLiveData()
    }
    fun getLoansGiven(): LiveData<List<Loan>> {
        return repository.getLoansByType(getCurrentUserId(), "GIVEN").asLiveData()
    }
    fun getLoansTaken(): LiveData<List<Loan>> {
        return repository.getLoansByType(getCurrentUserId(), "TAKEN").asLiveData()
    }
    fun getActiveLoans(): LiveData<List<Loan>> {
        return repository.getActiveLoans(getCurrentUserId()).asLiveData()
    }
    fun getTotalGivenAmount(): LiveData<Double?> {
        return repository.getTotalGivenAmount(getCurrentUserId()).asLiveData()
    }
    fun getTotalTakenAmount(): LiveData<Double?> {
        return repository.getTotalTakenAmount(getCurrentUserId()).asLiveData()
    }
    fun insertLoan(loan: Loan, onSuccess: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.insertLoan(loan)
            onSuccess(id)
        }
    }
    fun updateLoan(loan: Loan) {
        viewModelScope.launch {
            repository.updateLoan(loan)
        }
    }
    fun deleteLoan(loan: Loan) {
        viewModelScope.launch {
            repository.deleteLoan(loan)
        }
    }
    fun addPayment(loanId: Int, amount: Double, note: String = "", onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            val payment = LoanPayment(
                loanId = loanId,
                amount = amount,
                date = System.currentTimeMillis(),
                note = note
            )
            repository.addPayment(payment)
            val loan = repository.getLoanById(loanId)
            if (loan != null) {
                val totalPaid = repository.getTotalPaidAmount(loanId)
                val remaining = loan.amount - totalPaid
                val updatedLoan = loan.copy(
                    paidAmount = totalPaid,
                    status = when {
                        remaining <= 0 -> "SETTLED"
                        totalPaid > 0 -> "PARTIALLY_PAID"
                        else -> "ACTIVE"
                    }
                )
                repository.updateLoan(updatedLoan)
            }
            onComplete()
        }
    }
    fun getPaymentsForLoan(loanId: Int): LiveData<List<LoanPayment>> {
        return repository.getPaymentsForLoan(loanId).asLiveData()
    }
    suspend fun getLoanById(loanId: Int): Loan? {
        return repository.getLoanById(loanId)
    }
}
