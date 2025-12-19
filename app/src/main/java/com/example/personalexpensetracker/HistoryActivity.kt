package com.example.personalexpensetracker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HistoryActivity : AppCompatActivity() {

    private lateinit var vm: ExpenseViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExpenseAdapter
    private var allExpenses = listOf<Expense>()

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

        val etSearchCategory = findViewById<EditText>(R.id.etSearchCategory)
        val btnClearSearch = findViewById<ImageButton>(R.id.btnClearSearch)

        etSearchCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString().trim()
                if (query.isEmpty()) {
                    btnClearSearch.visibility = View.GONE
                    adapter.updateExpenses(allExpenses)
                } else {
                    btnClearSearch.visibility = View.VISIBLE
                    filterByCategory(query)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        btnClearSearch.setOnClickListener {
            etSearchCategory.text.clear()
            btnClearSearch.visibility = View.GONE
        }

        val fabFilter = findViewById<ExtendedFloatingActionButton>(R.id.fabFilter)
        fabFilter.setOnClickListener {
            showSortDialog()
        }

        vm.getAllExpensesForUser().observe(this) { expenses ->
            allExpenses = expenses
            adapter.updateExpenses(expenses)
        }
    }

    private fun filterByCategory(query: String) {
        val filtered = allExpenses.filter { expense ->
            expense.category.contains(query, ignoreCase = true)
        }
        adapter.updateExpenses(filtered)
        if (filtered.isEmpty()) {
            Toast.makeText(this, "No expenses found for '$query'", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Found ${filtered.size} expense(s)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf(
            "📄 Export to PDF",
            "📅 Filter by Date Range",
            "📅 Date: Newest First",
            "📅 Date: Oldest First",
            "💰 Amount: Highest First",
            "💰 Amount: Lowest First",
            "🔤 Category: A-Z"
        )

        AlertDialog.Builder(this)
            .setTitle("Sort & Filter Options")
            .setItems(sortOptions) { dialog, which ->
                when (which) {
                    0 -> downloadExpensesPdf()
                    1 -> showDateRangeDialog()
                    2 -> sortByDateDescending()
                    3 -> sortByDateAscending()
                    4 -> sortByAmountDescending()
                    5 -> sortByAmountAscending()
                    6 -> sortByCategoryAZ()
                }
                dialog.dismiss()
            }
            .show()
    }

    private fun showDateRangeDialog() {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 40, 50, 40)
        }

        val startDateText = TextView(this).apply {
            text = "Start Date:"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }

        val startDateButton = Button(this).apply {
            text = "Select Start Date"
            tag = 0L
        }

        val endDateText = TextView(this).apply {
            text = "End Date:"
            textSize = 16f
            setPadding(0, 30, 0, 10)
        }

        val endDateButton = Button(this).apply {
            text = "Select End Date"
            tag = System.currentTimeMillis()
        }

        startDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day, 0, 0, 0)
                    startDateButton.tag = calendar.timeInMillis
                    startDateButton.text = "$day/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        endDateButton.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day, 23, 59, 59)
                    endDateButton.tag = calendar.timeInMillis
                    endDateButton.text = "$day/${month + 1}/$year"
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        layout.addView(startDateText)
        layout.addView(startDateButton)
        layout.addView(endDateText)
        layout.addView(endDateButton)

        AlertDialog.Builder(this)
            .setTitle("Select Date Range")
            .setView(layout)
            .setPositiveButton("Apply") { dialog, _ ->
                val startDate = startDateButton.tag as? Long ?: 0L
                val endDate = endDateButton.tag as? Long ?: System.currentTimeMillis()
                filterByDateRange(startDate, endDate)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun filterByDateRange(startDate: Long, endDate: Long) {
        val filtered = allExpenses.filter { expense ->
            expense.date in startDate..endDate
        }
        adapter.updateExpenses(filtered)
        if (filtered.isEmpty()) {
            Toast.makeText(this, "No expenses in selected date range", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Found ${filtered.size} expense(s)", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sortByDateDescending() {
        val sorted = allExpenses.sortedByDescending { it.date }
        adapter.updateExpenses(sorted)
        Toast.makeText(this, "Sorted by Date: Newest First", Toast.LENGTH_SHORT).show()
    }

    private fun sortByDateAscending() {
        val sorted = allExpenses.sortedBy { it.date }
        adapter.updateExpenses(sorted)
        Toast.makeText(this, "Sorted by Date: Oldest First", Toast.LENGTH_SHORT).show()
    }

    private fun sortByAmountDescending() {
        val sorted = allExpenses.sortedByDescending { it.amount }
        adapter.updateExpenses(sorted)
        Toast.makeText(this, "Sorted by Amount: Highest First", Toast.LENGTH_SHORT).show()
    }

    private fun sortByAmountAscending() {
        val sorted = allExpenses.sortedBy { it.amount }
        adapter.updateExpenses(sorted)
        Toast.makeText(this, "Sorted by Amount: Lowest First", Toast.LENGTH_SHORT).show()
    }

    private fun sortByCategoryAZ() {
        val sorted = allExpenses.sortedBy { it.category }
        adapter.updateExpenses(sorted)
        Toast.makeText(this, "Sorted by Category: A-Z", Toast.LENGTH_SHORT).show()
    }

    private fun showDeleteConfirmationDialog(expense: Expense) {
        AlertDialog.Builder(this)
            .setTitle("Delete Expense")
            .setMessage("Are you sure you want to delete this expense?")
            .setPositiveButton("Delete") { dialog, _ ->
                vm.delete(expense)
                Toast.makeText(this, "Expense deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun downloadExpensesPdf() {
        try {
            val currentExpenses = allExpenses

            if (currentExpenses.isEmpty()) {
                Toast.makeText(this, "No expenses to export", Toast.LENGTH_SHORT).show()
                return
            }

            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val titlePaint = Paint().apply {
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = ContextCompat.getColor(this@HistoryActivity, R.color.primaryBlue)
            }

            val headerPaint = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = android.graphics.Color.BLACK
            }

            val normalPaint = Paint().apply {
                textSize = 12f
                color = android.graphics.Color.BLACK
            }

            var yPos = 50f

            canvas.drawText("Expense Report", 50f, yPos, titlePaint)
            yPos += 30f

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            canvas.drawText("Generated: ${dateFormat.format(Date())}", 50f, yPos, normalPaint)
            yPos += 30f

            val totalAmount = currentExpenses.sumOf { it.amount }
            canvas.drawText("Total: ₹${"%.2f".format(totalAmount)}", 50f, yPos, headerPaint)
            yPos += 20f

            canvas.drawText("Count: ${currentExpenses.size} expenses", 50f, yPos, normalPaint)
            yPos += 40f

            canvas.drawText("Category", 50f, yPos, headerPaint)
            canvas.drawText("Amount", 200f, yPos, headerPaint)
            canvas.drawText("Date", 350f, yPos, headerPaint)
            canvas.drawText("Note", 470f, yPos, headerPaint)
            yPos += 20f

            canvas.drawLine(50f, yPos, 545f, yPos, normalPaint)
            yPos += 20f

            var pageNumber = 1
            currentExpenses.forEach { expense ->
                if (yPos > 750f) {
                    canvas.drawText("Page $pageNumber", 270f, 820f, normalPaint)
                    pdfDocument.finishPage(page)
                    pageNumber++

                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pageNumber).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    val newCanvas: Canvas = newPage.canvas

                    yPos = 50f
                    newCanvas.drawText("Category", 50f, yPos, headerPaint)
                    newCanvas.drawText("Amount", 200f, yPos, headerPaint)
                    newCanvas.drawText("Date", 350f, yPos, headerPaint)
                    newCanvas.drawText("Note", 470f, yPos, headerPaint)
                    yPos += 20f

                    newCanvas.drawLine(50f, yPos, 545f, yPos, normalPaint)
                    yPos += 20f
                }

                canvas.drawText(expense.category, 50f, yPos, normalPaint)
                canvas.drawText("₹${"%.2f".format(expense.amount)}", 200f, yPos, normalPaint)
                canvas.drawText(dateFormat.format(Date(expense.date)), 350f, yPos, normalPaint)

                val noteText = expense.note ?: ""
                val note = if (noteText.length > 15) "${noteText.take(12)}..." else noteText
                canvas.drawText(note, 470f, yPos, normalPaint)

                yPos += 25f
            }

            canvas.drawText("Page $pageNumber", 270f, 820f, normalPaint)
            pdfDocument.finishPage(page)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val fileName = "Expenses_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(downloadsDir, fileName)

            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            Toast.makeText(this, "PDF saved to Downloads: $fileName", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error creating PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }
}
