package com.example.personalexpensetracker
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
class AdvancedAnalyticsActivity : AppCompatActivity() {
    private lateinit var vm: ExpenseViewModel
    private lateinit var lineChart: LineChart
    private lateinit var barChartWeekly: BarChart
    private lateinit var pieChartMonthly: PieChart
    private lateinit var tvWeekdayAvg: TextView
    private lateinit var tvWeekendAvg: TextView
    private lateinit var tvMonthlyTrend: TextView
    private lateinit var tvSpendingPattern: TextView
    private lateinit var tvTopCategory: TextView
    private lateinit var tvBudgetStatus: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_analytics)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { onBackPressed() }
        lineChart = findViewById(R.id.lineChartTrend)
        barChartWeekly = findViewById(R.id.barChartWeekly)
        pieChartMonthly = findViewById(R.id.pieChartMonthly)
        tvWeekdayAvg = findViewById(R.id.tvWeekdayAvg)
        tvWeekendAvg = findViewById(R.id.tvWeekendAvg)
        tvMonthlyTrend = findViewById(R.id.tvMonthlyTrend)
        tvSpendingPattern = findViewById(R.id.tvSpendingPattern)
        tvTopCategory = findViewById(R.id.tvTopCategory)
        tvBudgetStatus = findViewById(R.id.tvBudgetStatus)
        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]
        loadAdvancedAnalytics()
    }
    private fun loadAdvancedAnalytics() {
        vm.getAllExpensesForUser().observe(this) { expenses ->
            if (expenses.isEmpty()) {
                return@observe
            }
            calculateWeekdayVsWeekend(expenses)
            setupMonthlyTrendChart(expenses)
            setupWeeklyComparisonChart(expenses)
            setupMonthlyDistributionChart(expenses)
            analyzeSpendingPattern(expenses)
            analyzeBudgetStatus(expenses)
        }
    }
    private fun calculateWeekdayVsWeekend(expenses: List<Expense>) {
        val calendar = Calendar.getInstance()
        var weekdayTotal = 0.0
        var weekendTotal = 0.0
        var weekdayCount = 0
        var weekendCount = 0
        expenses.forEach { expense ->
            calendar.timeInMillis = expense.date
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                weekendTotal += expense.amount
                weekendCount++
            } else {
                weekdayTotal += expense.amount
                weekdayCount++
            }
        }
        val weekdayAvg = if (weekdayCount > 0) weekdayTotal / weekdayCount else 0.0
        val weekendAvg = if (weekendCount > 0) weekendTotal / weekendCount else 0.0
        tvWeekdayAvg.text = "₹${String.format("%.2f", weekdayAvg)}"
        tvWeekendAvg.text = "₹${String.format("%.2f", weekendAvg)}"
        val pattern = when {
            weekendAvg > weekdayAvg * 1.5 -> " High weekend spender"
            weekdayAvg > weekendAvg * 1.5 -> " Workday expenses dominate"
            else -> " Balanced spending pattern"
        }
        tvSpendingPattern.text = pattern
    }
    private fun setupMonthlyTrendChart(expenses: List<Expense>) {
        val calendar = Calendar.getInstance()
        val monthlyData = mutableMapOf<String, Float>()
        val dateFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())
        for (i in 5 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MONTH, -i)
            val monthKey = dateFormat.format(calendar.time)
            monthlyData[monthKey] = 0f
        }
        expenses.forEach { expense ->
            calendar.timeInMillis = expense.date
            val monthKey = dateFormat.format(calendar.time)
            if (monthKey in monthlyData) {
                monthlyData[monthKey] = monthlyData[monthKey]!! + expense.amount.toFloat()
            }
        }
        val entries = monthlyData.entries.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value)
        }
        val dataSet = LineDataSet(entries, "Monthly Spending Trend").apply {
            color = Color.parseColor("#2196F3")
            setCircleColor(Color.parseColor("#2196F3"))
            lineWidth = 3f
            circleRadius = 5f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.parseColor("#2196F3")
            fillAlpha = 50
        }
        lineChart.apply {
            data = LineData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(monthlyData.keys.toList())
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
        val values = monthlyData.values.toList()
        if (values.size >= 2) {
            val lastMonth = values.last()
            val previousMonth = values[values.size - 2]
            val change = ((lastMonth - previousMonth) / previousMonth * 100)
            val trend = when {
                change > 10 -> " Increasing (+${String.format("%.1f", change)}%)"
                change < -10 -> " Decreasing (${String.format("%.1f", change)}%)"
                else -> " Stable"
            }
            tvMonthlyTrend.text = trend
        }
    }
    private fun setupWeeklyComparisonChart(expenses: List<Expense>) {
        val calendar = Calendar.getInstance()
        val weeklyData = mutableMapOf<String, Float>()
        for (i in 3 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.WEEK_OF_YEAR, -i)
            val weekKey = "Week ${4 - i}"
            weeklyData[weekKey] = 0f
        }
        val fourWeeksAgo = System.currentTimeMillis() - (28L * 24 * 60 * 60 * 1000)
        expenses.filter { it.date >= fourWeeksAgo }.forEach { expense ->
            calendar.timeInMillis = expense.date
            val weeksAgo = ((System.currentTimeMillis() - expense.date) / (7L * 24 * 60 * 60 * 1000)).toInt()
            val weekKey = "Week ${4 - weeksAgo}"
            if (weekKey in weeklyData) {
                weeklyData[weekKey] = weeklyData[weekKey]!! + expense.amount.toFloat()
            }
        }
        val entries = weeklyData.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value)
        }
        val dataSet = BarDataSet(entries, "Weekly Comparison").apply {
            colors = listOf(
                Color.parseColor("#FF6B6B"),
                Color.parseColor("#4ECDC4"),
                Color.parseColor("#45B7D1"),
                Color.parseColor("#96CEB4")
            )
            setDrawValues(true)
            valueTextSize = 10f
        }
        barChartWeekly.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(weeklyData.keys.toList())
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }
    private fun setupMonthlyDistributionChart(expenses: List<Expense>) {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val monthlyExpenses = expenses.filter { expense ->
            calendar.timeInMillis = expense.date
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }
        val categoryTotals = monthlyExpenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount }.toFloat() }
            .toList()
            .sortedByDescending { it.second }
            .take(5)
        if (categoryTotals.isEmpty()) {
            return
        }
        val entries = categoryTotals.mapIndexed { index, (category, amount) ->
            PieEntry(amount, category)
        }
        val colors = listOf(
            Color.parseColor("#FF6B6B"),
            Color.parseColor("#4ECDC4"),
            Color.parseColor("#45B7D1"),
            Color.parseColor("#96CEB4"),
            Color.parseColor("#FECA57")
        )
        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }
        pieChartMonthly.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = true
            setDrawEntryLabels(true)
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
            animateY(1000)
            invalidate()
        }
        tvTopCategory.text = "${categoryTotals.first().first}\n₹${String.format("%.2f", categoryTotals.first().second)}"
    }
    private fun analyzeSpendingPattern(expenses: List<Expense>) {
    }
    private fun analyzeBudgetStatus(expenses: List<Expense>) {
        val currentUserId = vm.getCurrentUserId()
        val monthlyIncome = PrefsHelper.getMonthlyIncome(this, currentUserId)
        if (monthlyIncome <= 0) {
            tvBudgetStatus.text = "Set monthly income for insights"
            return
        }
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        val monthTotal = expenses.filter { expense ->
            calendar.timeInMillis = expense.date
            calendar.get(Calendar.MONTH) == currentMonth && 
            calendar.get(Calendar.YEAR) == currentYear
        }.sumOf { it.amount }
        val percentage = (monthTotal / monthlyIncome * 100).toInt()
        val status = when {
            percentage >= 90 -> " Critical (${percentage}% used)"
            percentage >= 70 -> " Warning (${percentage}% used)"
            percentage >= 50 -> " Good (${percentage}% used)"
            else -> " Excellent (${percentage}% used)"
        }
        tvBudgetStatus.text = status
    }
}