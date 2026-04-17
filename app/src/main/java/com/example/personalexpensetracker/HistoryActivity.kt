package com.example.personalexpensetracker

import android.app.AlertDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.content.ContentValues
import android.provider.MediaStore

class HistoryActivity : AppCompatActivity() {
    private lateinit var vm: ExpenseViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        vm = ViewModelProvider(this)[ExpenseViewModel::class.java]
        recyclerView = findViewById(R.id.rvHistory)

        val deleteCallback: (Expense) -> Unit = { expense ->
            showDeleteConfirmationDialog(expense)
        }

        adapter = ExpenseAdapter(emptyList(), deleteCallback)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        vm.getAllExpensesForUser().observe(this) { expenses ->
            adapter.updateExpenses(expenses)
        }

        // 🔥 TEMP: call export (you can later attach this to a button)
        exportPdf()
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        try {
            AlertDialog.Builder(this)
                .setTitle("Delete Expense")
                .setMessage(
                    "Are you sure you want to delete this expense?\n\n" +
                            "Category: ${expense.category}\n" +
                            "Amount: ₹${expense.amount.toInt()}"
                )
                .setPositiveButton("Delete") { dialog, _ ->
                    try {
                        vm.delete(expense)
                        Toast.makeText(this, "Expense deleted successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Error deleting expense: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error showing dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ✅ PDF EXPORT FUNCTION (added)
    private fun exportPdf() {
        val expenses = vm.getAllExpensesForUser().value ?: emptyList()

        if (expenses.isEmpty()) {
            Toast.makeText(this, "No data to export", Toast.LENGTH_SHORT).show()
            return
        }

        val fileName = "expenses_${System.currentTimeMillis()}.pdf"

        val resolver = contentResolver
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/")
        }

        val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)

        uri?.let {
            val outputStream = resolver.openOutputStream(it)

            if (outputStream != null) {
                val success = PdfExporter.exportExpensesToPdf(expenses, outputStream)

                if (success) {
                    Toast.makeText(this, "PDF saved in Documents", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Export failed", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}