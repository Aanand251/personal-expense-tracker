package com.example.personalexpensetracker

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.text.NumberFormat
import java.util.Locale
import androidx.navigation.fragment.findNavController






class MainActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter

    private lateinit var vm: ExpenseViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val recyclerView = findViewById<RecyclerView>(R.id.rvQuickActions)
        val tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        val rvQuickActions = findViewById<RecyclerView>(R.id.rvQuickActions)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
//        val tvTotalSavingsMain= findViewById<TextView>(R.id.tvTotalSavingsMain)
        val tvMonthlyIncome = findViewById<TextView>(R.id.tvMonthlyIncome)
        val tvTotalExpense = findViewById<TextView>(R.id.tvTotalExpense)
        val tvFixedExpense = findViewById<TextView>(R.id.tvFixedExpense)

        val tvTotalSavingsMain = findViewById<TextView>(R.id.tvTotalSavingsMain)

        val savingsViewModel = ViewModelProvider(this)[SavingsViewModel::class.java]

        savingsViewModel.getTotalSavingsForMainScreen().observe(this) { total ->
            val safeTotal = total ?: 0.0
            val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(safeTotal)
            tvTotalSavingsMain.text = "Total Savings : $formatted"
        }
//        vm.getTotalExpensesWithFixed().observe(this) { total ->
//            val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(total)
//            tvTotalExpense.text = "Total Expenses : $formatted"
//        }


        expenseAdapter = ExpenseAdapter(emptyList())
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = expenseAdapter



        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]


        vm.getFixedExpensesForUser().observe(this) { fixedExpenses ->
            expenseAdapter.updateList(fixedExpenses)
        }
        vm.getTotalExpensesForUser { totalExpense ->
            tvTotalExpense.text = "Total Expenses : ₹${totalExpense.toInt()}"
        }

        val btnOpenAnalytics = findViewById<LinearLayout>(R.id.btnOpenAnalytics)
        btnOpenAnalytics.setOnClickListener {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            navHostFragment.navController.navigate(R.id.analyticsFragment)
        }




            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {

                        Toast.makeText(this, "Already at Home", Toast.LENGTH_SHORT).show()
                        true
                    }

                    R.id.nav_add_expense -> {

                        startActivity(Intent(this, AddExpenseActivity::class.java))
                        true
                    }

                    R.id.nav_history -> {

                        startActivity(Intent(this, HistoryActivity::class.java))
                        true
                    }

                    R.id.nav_savings -> {

                        startActivity(Intent(this, SavingsActivity::class.java))
                        true
                    }

                    R.id.nav_settings -> {

                        startActivity(Intent(this, SettingsActivity::class.java))
                        true
                    }

                    else -> false
                }
            }
        }

    }


