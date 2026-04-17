package com.example.personalexpensetracker
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import java.text.NumberFormat
import java.util.Locale
data class CategoryPrediction(
    val category: String,
    val predictedAmount: Double,
    val trend: String, // "increasing", "decreasing", "stable"
    val confidenceScore: Int, // 0-100
    val lastMonthAmount: Double,
    val changePercentage: Double
)
data class PredictionResult(
    val totalPredicted: Double,
    val categoryPredictions: List<CategoryPrediction>,
    val hasEnoughData: Boolean,
    val monthsAnalyzed: Int
)
object PredictionHelper {
    private const val MIN_MONTHS_REQUIRED = 2
    fun generatePredictions(expenses: List<Expense>): PredictionResult {
        if (expenses.isEmpty()) {
            return PredictionResult(
                totalPredicted = 0.0,
                categoryPredictions = emptyList(),
                hasEnoughData = false,
                monthsAnalyzed = 0
            )
        }
        val monthlyData = groupByMonth(expenses)
        val monthsCount = monthlyData.size
        if (monthsCount < MIN_MONTHS_REQUIRED) {
            return PredictionResult(
                totalPredicted = 0.0,
                categoryPredictions = emptyList(),
                hasEnoughData = false,
                monthsAnalyzed = monthsCount
            )
        }
        val categories = expenses.map { it.category }.distinct()
        val categoryPredictions = categories.mapNotNull { category ->
            predictForCategory(category, expenses, monthlyData)
        }
        val totalPredicted = categoryPredictions.sumOf { it.predictedAmount }
        return PredictionResult(
            totalPredicted = totalPredicted,
            categoryPredictions = categoryPredictions.sortedByDescending { it.predictedAmount },
            hasEnoughData = true,
            monthsAnalyzed = monthsCount
        )
    }
    private fun groupByMonth(expenses: List<Expense>): Map<String, List<Expense>> {
        val calendar = Calendar.getInstance()
        return expenses.groupBy { expense ->
            calendar.timeInMillis = expense.date
            "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH) + 1)}"
        }
    }
    private fun predictForCategory(
        category: String,
        allExpenses: List<Expense>,
        monthlyData: Map<String, List<Expense>>
    ): CategoryPrediction? {
        val categoryExpenses = allExpenses.filter { it.category == category }
        if (categoryExpenses.isEmpty()) return null
        val monthlyTotals = monthlyData.keys.sorted().takeLast(6).mapNotNull { month ->
            val monthExpenses = monthlyData[month]?.filter { it.category == category }
            if (monthExpenses.isNullOrEmpty()) null
            else month to monthExpenses.sumOf { it.amount }
        }
        if (monthlyTotals.isEmpty()) return null
        val lastMonthAmount = monthlyTotals.lastOrNull()?.second ?: 0.0
        val amounts = monthlyTotals.map { it.second }
        val predictedAmount = calculatePrediction(amounts)
        val trend = calculateTrend(amounts)
        val confidenceScore = calculateConfidence(amounts)
        val changePercentage = if (lastMonthAmount > 0) {
            ((predictedAmount - lastMonthAmount) / lastMonthAmount) * 100
        } else 0.0
        return CategoryPrediction(
            category = category,
            predictedAmount = predictedAmount.coerceAtLeast(0.0),
            trend = trend,
            confidenceScore = confidenceScore,
            lastMonthAmount = lastMonthAmount,
            changePercentage = changePercentage
        )
    }
    private fun calculatePrediction(amounts: List<Double>): Double {
        if (amounts.isEmpty()) return 0.0
        if (amounts.size == 1) return amounts[0]
        val weights = amounts.indices.map { (it + 1).toDouble() }
        val weightedSum = amounts.zip(weights) { amount, weight -> amount * weight }.sum()
        val totalWeight = weights.sum()
        val weightedAverage = weightedSum / totalWeight
        val trendFactor = calculateTrendFactor(amounts)
        return weightedAverage * trendFactor
    }
    private fun calculateTrendFactor(amounts: List<Double>): Double {
        if (amounts.size < 2) return 1.0
        val n = amounts.size
        val x = (1..n).map { it.toDouble() }
        val y = amounts
        val xMean = x.average()
        val yMean = y.average()
        val numerator = x.zip(y) { xi, yi -> (xi - xMean) * (yi - yMean) }.sum()
        val denominator = x.sumOf { (it - xMean).pow(2) }
        if (denominator == 0.0) return 1.0
        val slope = numerator / denominator
        val avgAmount = yMean
        if (avgAmount == 0.0) return 1.0
        val trendPercentage = (slope / avgAmount)
        return 1.0 + trendPercentage
    }
    private fun calculateTrend(amounts: List<Double>): String {
        if (amounts.size < 2) return "stable"
        val first = amounts.take(amounts.size / 2).average()
        val second = amounts.takeLast(amounts.size / 2).average()
        val changePercentage = if (first > 0) ((second - first) / first) * 100 else 0.0
        return when {
            changePercentage > 5 -> "increasing"
            changePercentage < -5 -> "decreasing"
            else -> "stable"
        }
    }
    private fun calculateConfidence(amounts: List<Double>): Int {
        if (amounts.size < 2) return 50
        val mean = amounts.average()
        if (mean == 0.0) return 50
        val variance = amounts.sumOf { (it - mean).pow(2) } / amounts.size
        val stdDev = sqrt(variance)
        val cv = (stdDev / mean) * 100
        val baseConfidence = when {
            cv < 10 -> 95  // Very consistent
            cv < 20 -> 85  // Consistent
            cv < 30 -> 75  // Moderately consistent
            cv < 40 -> 65  // Somewhat variable
            cv < 50 -> 55  // Variable
            else -> 45     // Highly variable
        }
        val dataBonus = minOf(amounts.size * 2, 10)
        return (baseConfidence + dataBonus).coerceIn(0, 100)
    }
    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
        return format.format(amount)
    }
    fun getTrendEmoji(trend: String): String {
        return when (trend) {
            "increasing" -> "📈"
            "decreasing" -> "📉"
            else -> "➡️"
        }
    }
    fun getConfidenceColor(score: Int): String {
        return when {
            score >= 80 -> "#4CAF50"  // Green
            score >= 60 -> "#FFC107"  // Amber
            else -> "#FF5722"         // Red
        }
    }
}
