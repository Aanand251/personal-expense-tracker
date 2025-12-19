package com.example.personalexpensetracker
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Dao
interface LoanDao {
    @Insert
    suspend fun insertLoan(loan: Loan): Long
    @Update
    suspend fun updateLoan(loan: Loan)
    @Delete
    suspend fun deleteLoan(loan: Loan)
    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY date DESC")
    fun getAllLoans(userId: String): Flow<List<Loan>>
    @Query("SELECT * FROM loans WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getLoansByType(userId: String, type: String): Flow<List<Loan>>
    @Query("SELECT * FROM loans WHERE userId = :userId AND status = 'ACTIVE' OR status = 'PARTIALLY_PAID' ORDER BY dueDate ASC")
    fun getActiveLoans(userId: String): Flow<List<Loan>>
    @Query("SELECT * FROM loans WHERE id = :loanId")
    suspend fun getLoanById(loanId: Int): Loan?
    @Query("SELECT SUM(amount - paidAmount) FROM loans WHERE userId = :userId AND type = 'GIVEN' AND status != 'SETTLED'")
    fun getTotalGivenAmount(userId: String): Flow<Double?>
    @Query("SELECT SUM(amount - paidAmount) FROM loans WHERE userId = :userId AND type = 'TAKEN' AND status != 'SETTLED'")
    fun getTotalTakenAmount(userId: String): Flow<Double?>
    @Insert
    suspend fun insertPayment(payment: LoanPayment)
    @Query("SELECT * FROM loan_payments WHERE loanId = :loanId ORDER BY date DESC")
    fun getPaymentsForLoan(loanId: Int): Flow<List<LoanPayment>>
    @Query("SELECT SUM(amount) FROM loan_payments WHERE loanId = :loanId")
    suspend fun getTotalPaidAmount(loanId: Int): Double?
}
