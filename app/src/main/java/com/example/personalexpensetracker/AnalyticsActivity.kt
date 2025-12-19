package com.example.personalexpensetracker
import android.graphics.Color
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.text.NumberFormat
import java.util.Locale
class AnalyticsActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var barChart: BarChart
    private lateinit var vm: ExpenseViewModel
    private lateinit var tvTotalExpenses: TextView
    private lateinit var tvHighestCategory: TextView
    private lateinit var tvLowestCategory: TextView
    private lateinit var tvAverageExpense: TextView
    private lateinit var tvTotalTransactions: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Analytics & Insights"
        pieChart = findViewById(R.id.pieChart)
        barChart = findViewById(R.id.barChart)
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses)
        tvHighestCategory = findViewById(R.id.tvHighestCategory)
        tvLowestCategory = findViewById(R.id.tvLowestCategory)
        tvAverageExpense = findViewById(R.id.tvAverageExpense)
        tvTotalTransactions = findViewById(R.id.tvTotalTransactions)
        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnAdvancedAnalytics).setOnClickListener {
            val intent = android.content.Intent(this, AdvancedAnalyticsActivity::class.java)
            startActivity(intent)
        }
        findViewById<com.google.android.material.card.MaterialCardView>(R.id.btnExpensePrediction).setOnClickListener {
            val intent = android.content.Intent(this, ExpensePredictionActivity::class.java)
            startActivity(intent)
        }
        setupPieChart()
        setupBarChart()
        loadAnalyticsData()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
    private fun setupPieChart() {
        pieChart.apply {
            description.isEnabled = false
            isRotationEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setTransparentCircleAlpha(0)
            holeRadius = 45f
            transparentCircleRadius = 50f
            setDrawCenterText(true)
            centerText = "Expenses\nby Category"
            setCenterTextSize(14f)
            setCenterTextColor(Color.BLACK)
            legend.isEnabled = true
            legend.textSize = 12f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(10f)
        }
    }
    private fun setupBarChart() {
        barChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            legend.isEnabled = false
            axisLeft.setDrawGridLines(false)
            axisRight.isEnabled = false
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f
            animateY(1000)
        }
    }
    private fun loadAnalyticsData() {
        vm.getAllExpensesForUser().observe(this) { expenseList ->
            if (expenseList.isEmpty()) {
                pieChart.centerText = "No Data\nAvailable"
                tvTotalExpenses.text = "Total: â‚¹0"
                tvTotalTransactions.text = "Transactions: 0"
                tvAverageExpense.text = "Average: â‚¹0"
                tvHighestCategory.text = "Highest: -"
                tvLowestCategory.text = "Lowest: -"
                return@observe
            }
            val totalExpenses = expenseList.sumOf { it.amount }
            val transactionCount = expenseList.size
            val averageExpense = totalExpenses / transactionCount
            val grouped = expenseList.groupBy { it.category }
            val categoryTotals = grouped.mapValues { (_, expenses) -> 
                expenses.sumOf { it.amount }
            }
            val highestCategory = categoryTotals.maxByOrNull { it.value }
            val lowestCategory = categoryTotals.minByOrNull { it.value }
            val formatter = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
            tvTotalExpenses.text = "Total: ${formatter.format(totalExpenses)}"
            tvTotalTransactions.text = "Transactions: $transactionCount"
            tvAverageExpense.text = "Average: ${formatter.format(averageExpense)}"
            tvHighestCategory.text = "Highest: ${highestCategory?.key ?: "-"} (${formatter.format(highestCategory?.value ?: 0)})"
            tvLowestCategory.text = "Lowest: ${lowestCategory?.key ?: "-"} (${formatter.format(lowestCategory?.value ?: 0)})"
            val pieEntries = categoryTotals.map { (category, total) ->
                PieEntry(total.toFloat(), category)
            }
            val pieDataSet = PieDataSet(pieEntries, "Expense Categories")
            pieDataSet.colors = listOf(
                Color.parseColor("#F44336"), // Red
                Color.parseColor("#4CAF50"), // Green
                Color.parseColor("#2196F3"), // Blue
                Color.parseColor("#FFEB3B"), // Yellow
                Color.parseColor("#9C27B0"), // Purple
                Color.parseColor("#FF9800"), // Orange
                Color.parseColor("#00BCD4"), // Cyan
                Color.parseColor("#795548")  // Brown
            )
            pieDataSet.valueTextSize = 12f
            pieDataSet.valueTextColor = Color.WHITE
            pieDataSet.sliceSpace = 3f
            val pieData = PieData(pieDataSet)
            pieChart.data = pieData
            pieChart.animateY(1000)
            pieChart.invalidate()
            val barEntries = categoryTotals.entries.mapIndexed { index, entry ->
                BarEntry(index.toFloat(), entry.value.toFloat())
            }
            val barDataSet = BarDataSet(barEntries, "Categories")
            barDataSet.colors = listOf(
                Color.parseColor("#F44336"),
                Color.parseColor("#4CAF50"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#FFEB3B"),
                Color.parseColor("#9C27B0"),
                Color.parseColor("#FF9800"),
                Color.parseColor("#00BCD4"),
                Color.parseColor("#795548")
            )
            barDataSet.valueTextSize = 12f
            barDataSet.valueTextColor = Color.BLACK
            val barData = BarData(barDataSet)
            barChart.data = barData
            barChart.xAxis.valueFormatter = IndexAxisValueFormatter(categoryTotals.keys.toList())
            barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
            barChart.invalidate()
        }
    }
}
