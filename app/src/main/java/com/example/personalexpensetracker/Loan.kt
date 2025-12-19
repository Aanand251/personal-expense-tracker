package com.example.personalexpensetracker
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "loans")
data class Loan(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userId: String,
    val personName: String,
    val amount: Double,
    val type: String,
    val date: Long,
    val dueDate: Long,
    val interestRate: Double = 0.0,
    val status: String,
    val paidAmount: Double = 0.0,
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
@Entity(tableName = "loan_payments")
data class LoanPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val loanId: Int,
    val amount: Double,
    val date: Long,
    val note: String = ""
)
