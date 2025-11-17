package com.example.personalexpensetracker
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "savings")
data class Savings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String,
    val amount: Double,
    val date: Long,
    val note: String? = null
)
