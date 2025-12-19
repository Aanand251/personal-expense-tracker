package com.example.personalexpensetracker
import androidx.room.Entity
import androidx.room.PrimaryKey
@Entity(tableName = "expense")
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val title: String,
    val amount: Double,
    val date: Long,
    val isFixed: Boolean = false,
    val note: String? = null,
    val name: String? = null,
    val category: String
)
