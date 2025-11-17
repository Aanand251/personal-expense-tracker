package com.example.personalexpensetracker

import android.content.Context

object PrefsHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_MONTHLY_INCOME = "monthly_income"
    private const val KEY_FIXED_EXPENSE = "fixed_expense"

    fun saveMonthlyIncome(context: Context, income: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MONTHLY_INCOME, income.toString()).apply()
    }

    fun getMonthlyIncome(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_MONTHLY_INCOME, "0.0")?.toDoubleOrNull() ?: 0.0
    }

    fun saveFixedExpense(context: Context, expense: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_FIXED_EXPENSE, expense.toString()).apply()
    }

    fun getFixedExpense(context: Context): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_FIXED_EXPENSE, "0.0")?.toDoubleOrNull() ?: 0.0
    }
}
