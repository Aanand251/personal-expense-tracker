package com.example.personalexpensetracker
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.card.MaterialCardView
class ExpensePredictionActivity : AppCompatActivity() {
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var tvTotalPredicted: TextView
    private lateinit var tvMonthsAnalyzed: TextView
    private lateinit var llCategoryPredictions: LinearLayout
    private lateinit var chartPrediction: LineChart
    private lateinit var cardNoData: MaterialCardView
    private lateinit var cardPredictionContent: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_prediction)
        initializeViews()
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Expense Predictions"
        expenseViewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        loadPredictions()
    }
    private fun initializeViews() {
        tvTotalPredicted = findViewById(R.id.tvTotalPredicted)
        tvMonthsAnalyzed = findViewById(R.id.tvMonthsAnalyzed)
        llCategoryPredictions = findViewById(R.id.llCategoryPredictions)
        chartPrediction = findViewById(R.id.chartPrediction)
        cardNoData = findViewById(R.id.cardNoData)
        cardPredictionContent = findViewById(R.id.cardPredictionContent)
    }
    private fun loadPredictions() {
        expenseViewModel.getAllExpensesForUser().observe(this) { expenses ->
            if (expenses.isNullOrEmpty()) {
                showNoDataState()
                return@observe
            }
            val predictionResult = PredictionHelper.generatePredictions(expenses)
            if (!predictionResult.hasEnoughData) {
                showInsufficientDataState(predictionResult.monthsAnalyzed)
                return@observe
            }
            showPredictions(predictionResult)
        }
    }
    private fun showNoDataState() {
        cardNoData.visibility = View.VISIBLE
        cardPredictionContent.visibility = View.GONE
        findViewById<TextView>(R.id.tvNoDataMessage).text = 
            "No expense data found.\n\nStart adding expenses to see predictions!"
    }
    private fun showInsufficientDataState(monthsFound: Int) {
        cardNoData.visibility = View.VISIBLE
        cardPredictionContent.visibility = View.GONE
        findViewById<TextView>(R.id.tvNoDataMessage).text = 
            "Need more data for predictions.\n\n" +
            "You have $monthsFound month(s) of data.\n" +
            "Minimum 2 months required."
    }
    private fun showPredictions(result: PredictionResult) {
        cardNoData.visibility = View.GONE
        cardPredictionContent.visibility = View.VISIBLE
        tvTotalPredicted.text = PredictionHelper.formatCurrency(result.totalPredicted)
        tvMonthsAnalyzed.text = "Based on ${result.monthsAnalyzed} months of data"
        llCategoryPredictions.removeAllViews()
        result.categoryPredictions.forEach { prediction ->
            addCategoryPredictionCard(prediction)
        }
        setupPredictionChart(result)
    }
    private fun addCategoryPredictionCard(prediction: CategoryPrediction) {
        val cardView = layoutInflater.inflate(
            R.layout.item_category_prediction,
            llCategoryPredictions,
            false
        ) as MaterialCardView
        cardView.findViewById<TextView>(R.id.tvCategoryName).text = prediction.category
        cardView.findViewById<TextView>(R.id.tvPredictedAmount).text = 
            PredictionHelper.formatCurrency(prediction.predictedAmount)
        val tvTrend = cardView.findViewById<TextView>(R.id.tvTrend)
        val trendEmoji = PredictionHelper.getTrendEmoji(prediction.trend)
        tvTrend.text = "$trendEmoji ${prediction.trend.replaceFirstChar { it.uppercase() }}"
        when (prediction.trend) {
            "increasing" -> tvTrend.setTextColor(Color.parseColor("#F44336"))
            "decreasing" -> tvTrend.setTextColor(Color.parseColor("#4CAF50"))
            else -> tvTrend.setTextColor(Color.parseColor("#9E9E9E"))
        }
        val tvChange = cardView.findViewById<TextView>(R.id.tvChangePercentage)
        val changeText = if (prediction.changePercentage >= 0) {
            "+%.1f%%".format(prediction.changePercentage)
        } else {
            "%.1f%%".format(prediction.changePercentage)
        }
        tvChange.text = "vs last month: $changeText"
        val tvConfidence = cardView.findViewById<TextView>(R.id.tvConfidence)
        tvConfidence.text = "Confidence: ${prediction.confidenceScore}%"
        val confidenceColor = PredictionHelper.getConfidenceColor(prediction.confidenceScore)
        tvConfidence.setTextColor(Color.parseColor(confidenceColor))
        cardView.findViewById<TextView>(R.id.tvLastMonth).text = 
            "Last month: ${PredictionHelper.formatCurrency(prediction.lastMonthAmount)}"
        llCategoryPredictions.addView(cardView)
    }
    private fun setupPredictionChart(result: PredictionResult) {
        val entries = ArrayList<Entry>()
        result.categoryPredictions.forEachIndexed { index, prediction ->
            entries.add(Entry(index.toFloat(), prediction.predictedAmount.toFloat()))
        }
        if (entries.isEmpty()) {
            chartPrediction.visibility = View.GONE
            return
        }
        chartPrediction.visibility = View.VISIBLE
        val dataSet = LineDataSet(entries, "Predicted Expenses by Category")
        dataSet.color = Color.parseColor("#2196F3")
        dataSet.setCircleColor(Color.parseColor("#2196F3"))
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 6f
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.BLACK
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER
        val lineData = LineData(dataSet)
        chartPrediction.data = lineData
        chartPrediction.description.isEnabled = false
        chartPrediction.legend.textSize = 12f
        chartPrediction.setDrawGridBackground(false)
        chartPrediction.animateY(1000)
        val xAxis = chartPrediction.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.textSize = 10f
        chartPrediction.axisLeft.textSize = 10f
        chartPrediction.axisRight.isEnabled = false
        chartPrediction.invalidate()
    }
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
