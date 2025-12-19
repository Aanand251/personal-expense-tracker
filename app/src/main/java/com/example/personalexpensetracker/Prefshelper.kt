package com.example.personalexpensetracker
import android.content.Context
object PrefsHelper {
    private const val PREFS_NAME = "app_prefs"
    private const val KEY_MONTHLY_INCOME = "monthly_income"
    private const val KEY_FIXED_EXPENSE = "fixed_expense"
    fun saveMonthlyIncome(context: Context, userId: String, income: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("${KEY_MONTHLY_INCOME}_$userId", income.toString()).apply()
    }
    fun getMonthlyIncome(context: Context, userId: String): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("${KEY_MONTHLY_INCOME}_$userId", "0.0")?.toDoubleOrNull() ?: 0.0
    }
    fun saveFixedExpense(context: Context, userId: String, expense: Double) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString("${KEY_FIXED_EXPENSE}_$userId", expense.toString()).apply()
    }
    fun getFixedExpense(context: Context, userId: String): Double {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString("${KEY_FIXED_EXPENSE}_$userId", "0.0")?.toDoubleOrNull() ?: 0.0
    }
}
