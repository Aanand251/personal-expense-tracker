package com.example.personalexpensetracker
import kotlinx.coroutines.flow.Flow
class LoanRepository(private val loanDao: LoanDao) {
    fun getAllLoans(userId: String): Flow<List<Loan>> {
        return loanDao.getAllLoans(userId)
    }
    fun getLoansByType(userId: String, type: String): Flow<List<Loan>> {
        return loanDao.getLoansByType(userId, type)
    }
    fun getActiveLoans(userId: String): Flow<List<Loan>> {
        return loanDao.getActiveLoans(userId)
    }
    suspend fun getLoanById(loanId: Int): Loan? {
        return loanDao.getLoanById(loanId)
    }
    fun getTotalGivenAmount(userId: String): Flow<Double?> {
        return loanDao.getTotalGivenAmount(userId)
    }
    fun getTotalTakenAmount(userId: String): Flow<Double?> {
        return loanDao.getTotalTakenAmount(userId)
    }
    suspend fun insertLoan(loan: Loan): Long {
        return loanDao.insertLoan(loan)
    }
    suspend fun updateLoan(loan: Loan) {
        loanDao.updateLoan(loan)
    }
    suspend fun deleteLoan(loan: Loan) {
        loanDao.deleteLoan(loan)
    }
    suspend fun addPayment(payment: LoanPayment) {
        loanDao.insertPayment(payment)
    }
    fun getPaymentsForLoan(loanId: Int): Flow<List<LoanPayment>> {
        return loanDao.getPaymentsForLoan(loanId)
    }
    suspend fun getTotalPaidAmount(loanId: Int): Double {
        return loanDao.getTotalPaidAmount(loanId) ?: 0.0
    }
}
