package com.example.personalexpensetracker

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.google.android.material.card.MaterialCardView
import java.text.NumberFormat
import java.util.Locale
import android.graphics.Color


class MainActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var vm: ExpenseViewModel
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ExpenseManager.createNotificationChannel(this)

        val recyclerView = findViewById<RecyclerView>(R.id.rvQuickActions)
        val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)
        val tvTotalSavingsMain = findViewById<TextView>(R.id.tvTotalSavingsMain)

        val savingsViewModel = ViewModelProvider(this)[SavingsViewModel::class.java]

        savingsViewModel.getTotalSavingsForMainScreen().observe(this) { total ->
            val safeTotal = total ?: 0.0
            val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(safeTotal)
            tvTotalSavingsMain.text = formatted.replace("₹", "₹ ")
        }

        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]

        expenseAdapter = ExpenseAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter

        vm.getFixedExpensesForUser().observe(this) { fixedExpenses ->
            expenseAdapter.updateExpenses(fixedExpenses)
        }

        vm.getTotalExpensesForUser { totalExpense ->
            val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalExpense)
            tvTotalExpense.text = formatted.replace("₹", "₹ ")
        }

        updateMonthlyIncomeDisplay()
        updateFixedExpenseDisplay()
        updateTotalExpenseDisplay()
        updateBudgetAlertCard()


        findViewById<MaterialCardView>(R.id.btnAnalyticsCard)?.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }


        val btnOpenAnalytics = findViewById<LinearLayout>(R.id.btnOpenAnalytics)
        btnOpenAnalytics?.setOnClickListener {
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnLoanCard)?.setOnClickListener {
            startActivity(Intent(this, LoanActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.btnReceiptScannerCard)?.setOnClickListener {
            startActivity(Intent(this, ReceiptScannerActivity::class.java))
        }

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navHome = findViewById<TextView>(R.id.navHome)
        val navExpenses = findViewById<TextView>(R.id.navExpenses)
        val navHistory = findViewById<TextView>(R.id.navHistory)
        val navSavings = findViewById<TextView>(R.id.navSavings)
        val navSettings = findViewById<TextView>(R.id.navSettings)

        navHome.setOnClickListener {

        }

        navExpenses.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }

        navHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        navSavings.setOnClickListener {
            startActivity(Intent(this, SavingsActivity::class.java))
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateMonthlyIncomeDisplay() {
        val tvMonthlyIncome = findViewById<TextView>(R.id.tvMonthlyIncome)
        val userId = vm.getCurrentUserId()
        val monthlyIncome = PrefsHelper.getMonthlyIncome(this, userId)
        val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(monthlyIncome)
        tvMonthlyIncome.text = formatted.replace("₹", "₹ ")
    }

    private fun updateFixedExpenseDisplay() {
        val tvFixedExpense = findViewById<TextView>(R.id.tvFixedExpense)
        val userId = vm.getCurrentUserId()
        val fixedExpense = PrefsHelper.getFixedExpense(this, userId)
        val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(fixedExpense)
        tvFixedExpense.text = formatted.replace("₹", "₹ ")
    }

    private fun updateTotalExpenseDisplay() {
        vm.getAllExpensesForUser().observe(this) { expenses ->
            val grandTotal = expenses.sumOf { it.amount.toDouble() }
            val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)
            tvTotalExpense.text = "Total Expenses : ₹${grandTotal.toInt()}"
        }
    }

    private fun updateBudgetAlertCard() {
        val budgetAlertCard = findViewById<MaterialCardView>(R.id.budgetAlertCard)
        val tvBudgetBadge = findViewById<TextView>(R.id.tvBudgetBadge)
        val tvBudgetMessage = findViewById<TextView>(R.id.tvBudgetMessage)
        val tvBudgetPercentage = findViewById<TextView>(R.id.tvBudgetPercentage)
        val tvBudgetSubtitle = findViewById<TextView>(R.id.tvBudgetSubtitle)
        val progressBudget = findViewById<ProgressBar>(R.id.progressBudget)
        val tvBudgetSpent = findViewById<TextView>(R.id.tvBudgetSpent)
        val tvBudgetRemaining = findViewById<TextView>(R.id.tvBudgetRemaining)
        val tvBudgetDaysLeft = findViewById<TextView>(R.id.tvBudgetDaysLeft)

        val userId = vm.getCurrentUserId()
        val monthlyIncome = PrefsHelper.getMonthlyIncome(this, userId)
        val fixedExpense = PrefsHelper.getFixedExpense(this, userId)
        val budget = monthlyIncome - fixedExpense

        vm.getAllExpensesForUser().observe(this) { expenses ->
            val totalExpense = expenses.sumOf { it.amount.toDouble() }
            val percentageSpent = if (budget > 0) ((totalExpense / budget) * 100).toInt() else 0
            val remaining = budget - totalExpense


            val formattedSpent = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(totalExpense)
            tvBudgetSpent.text = "Spent: ${formattedSpent.replace("₹", "₹")}"


            val formattedRemaining = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(if (remaining > 0) remaining else 0.0)
            tvBudgetRemaining.text = "Remaining: ${formattedRemaining.replace("₹", "₹")}"


            val calendar = java.util.Calendar.getInstance()
            val currentDay = calendar.get(java.util.Calendar.DAY_OF_MONTH)
            val daysInMonth = calendar.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
            val daysLeft = daysInMonth - currentDay
            tvBudgetDaysLeft.text = "$daysLeft days left"

            tvBudgetPercentage.text = "$percentageSpent%"
            progressBudget.progress = percentageSpent.coerceIn(0, 100)

            when {
                percentageSpent < 70 -> {
                    tvBudgetBadge.text = "Good ✓"
                    tvBudgetBadge.setBackgroundResource(R.drawable.badge_background_green)
                    tvBudgetPercentage.setTextColor(Color.parseColor("#4CAF50"))
                    progressBudget.progressDrawable = getProgressDrawable("#4CAF50")
                    tvBudgetMessage.text = "You're doing great! Keep up the good spending habits! 🎉"
                    tvBudgetMessage.setBackgroundResource(R.drawable.message_background)
                    tvBudgetSubtitle.text = "Well within budget"
                }
                percentageSpent < 90 -> {
                    tvBudgetBadge.text = "Warning ⚠"
                    tvBudgetBadge.setBackgroundResource(R.drawable.badge_background_yellow)
                    tvBudgetPercentage.setTextColor(Color.parseColor("#FF9800"))
                    progressBudget.progressDrawable = getProgressDrawable("#FF9800")
                    tvBudgetMessage.text = "Watch your spending! You're approaching your budget limit. 💡"
                    tvBudgetMessage.setBackgroundResource(R.drawable.message_background)
                    tvBudgetSubtitle.text = "Approaching budget limit"
                }
                else -> {
                    tvBudgetBadge.text = "Alert ⚠️"
                    tvBudgetBadge.setBackgroundResource(R.drawable.badge_background_red)
                    tvBudgetPercentage.setTextColor(Color.parseColor("#F44336"))
                    progressBudget.progressDrawable = getProgressDrawable("#F44336")
                    if (percentageSpent >= 100) {
                        tvBudgetMessage.text = "Budget exceeded! Consider reducing expenses. 🚨"
                        tvBudgetSubtitle.text = "Over budget"
                    } else {
                        tvBudgetMessage.text = "Critical! You're very close to exceeding your budget. ⚠️"
                        tvBudgetSubtitle.text = "Near budget limit"
                    }
                    tvBudgetMessage.setBackgroundResource(R.drawable.message_background)
                }
            }
        }
    }

    private fun getProgressDrawable(color: String): android.graphics.drawable.Drawable {
        val drawable = ContextCompat.getDrawable(this, R.drawable.progress_bar_budget)
        drawable?.setTint(Color.parseColor(color))
        return drawable!!
    }
}