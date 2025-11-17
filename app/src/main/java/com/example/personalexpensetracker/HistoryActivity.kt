package com.example.personalexpensetracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HistoryActivity : AppCompatActivity() {

    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var vm: ExpenseViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var filterButton: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recyclerView = findViewById(R.id.rvHistory)
        filterButton = findViewById(R.id.fabFilter)

        expenseAdapter = ExpenseAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = expenseAdapter

        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]

        // Initially load all expenses for user
        vm.getAllExpensesForUser().observe(this) { expenseList ->
            expenseAdapter.updateList(expenseList)
        }

        // Setup date range picker on filter button
        val dateRangePicker = MaterialDatePicker.Builder.dateRangePicker().build()

        filterButton.setOnClickListener {
            dateRangePicker.show(supportFragmentManager, "date_range_picker")
        }

        dateRangePicker.addOnPositiveButtonClickListener { selection ->
            val startDate = selection.first ?: 0L
            val endDate = selection.second ?: System.currentTimeMillis()
            val uid = vm.getCurrentUserId() // Assumed userId fetching method in viewmodel

            vm.getExpensesInRange(uid, startDate, endDate).observe(this) { filteredList ->
                expenseAdapter.updateList(filteredList)
            }
        }
    }
}
