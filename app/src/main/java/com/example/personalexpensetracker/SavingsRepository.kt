package com.example.personalexpensetracker

import kotlinx.coroutines.flow.Flow

class SavingsRepository(private val savingsDao: SavingsDao) {

    suspend fun insert(saving: Savings) {
        savingsDao.insert(saving)
    }

    suspend fun delete(saving: Savings) {
        savingsDao.delete(saving)
    }

    fun getTotalSavings(userId: String): Flow<Double?> {
        return savingsDao.getTotalSavings(userId)
    }

//    fun getAllSavings(userId: String): Flow<List<Savings>> {
//        return savingsDao.getAllSavings(userId)
//    }
    fun getTotalSavingsForMainScreen(userId: String): Flow<Double?> {
        return savingsDao.getTotalSavings(userId)
    }

}
