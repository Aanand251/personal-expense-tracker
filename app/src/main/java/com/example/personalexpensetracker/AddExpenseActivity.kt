package com.example.personalexpensetracker

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AddExpenseActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)

        val etAmount = findViewById<TextInputEditText>(R.id.etAmount)
        val etCategory = findViewById<AutoCompleteTextView>(R.id.etCategory)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        val etNote = findViewById<TextInputEditText>(R.id.etNote)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)

        val categories = listOf("Food", "Transport", "Shopping", "Recharge", "Bills", "Other")
        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(categoryAdapter)

        var selectedDateMillis: Long = System.currentTimeMillis()
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        etDate.setText(formatter.format(Date(selectedDateMillis)))
        etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateMillis = calendar.timeInMillis
                    etDate.setText(formatter.format(Date(selectedDateMillis)))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val category = etCategory.text.toString().trim()
            val note = etNote.text?.toString()?.trim() ?: ""
            val amount = amountStr.toDoubleOrNull() ?: 0.0

            if (amount <= 0 || category.isEmpty()) {
                Toast.makeText(this, "Amount & Category required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

            val newExpense = Expense(
                title = category,
                userId = currentUserId,
                amount = amount,
                date = selectedDateMillis,
                isFixed = false,
                note = note,
                category = category
            )

            val expenseViewModel = ViewModelProvider(this)
                .get(ExpenseViewModel::class.java)

            expenseViewModel.insert(newExpense)
            Toast.makeText(this, "Expense added!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
