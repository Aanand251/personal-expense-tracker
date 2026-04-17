package com.example.personalexpensetracker
import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
class LoanActivity : AppCompatActivity() {
    private lateinit var viewModel: LoanViewModel
    private lateinit var adapter: LoanAdapter
    private lateinit var rvLoans: RecyclerView
    private lateinit var tvTotalGiven: TextView
    private lateinit var tvTotalTaken: TextView
    private lateinit var tabLayout: TabLayout
    private var currentFilter = "ALL" // ALL, GIVEN, TAKEN
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loan)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        tvTotalGiven = findViewById(R.id.tvTotalGiven)
        tvTotalTaken = findViewById(R.id.tvTotalTaken)
        rvLoans = findViewById(R.id.rvLoans)
        tabLayout = findViewById(R.id.tabLayout)
        val fabAddLoan = findViewById<FloatingActionButton>(R.id.fabAddLoan)
        viewModel = ViewModelProvider(this)[LoanViewModel::class.java]
        adapter = LoanAdapter(
            loans = emptyList(),
            onAddPaymentClick = { loan -> showAddPaymentDialog(loan) },
            onDetailsClick = { loan -> showLoanDetailsDialog(loan) }
        )
        rvLoans.layoutManager = LinearLayoutManager(this)
        rvLoans.adapter = adapter
        tabLayout.addTab(tabLayout.newTab().setText("All"))
        tabLayout.addTab(tabLayout.newTab().setText("Money Lent"))
        tabLayout.addTab(tabLayout.newTab().setText("Money Borrowed"))
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> {
                        currentFilter = "ALL"
                        loadLoans()
                    }
                    1 -> {
                        currentFilter = "GIVEN"
                        loadLoansGiven()
                    }
                    2 -> {
                        currentFilter = "TAKEN"
                        loadLoansTaken()
                    }
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        loadSummary()
        loadLoans()
        fabAddLoan.setOnClickListener {
            showAddLoanDialog()
        }
    }
    private fun loadSummary() {
        viewModel.getTotalGivenAmount().observe(this) { amount ->
            tvTotalGiven.text = currencyFormat.format(amount ?: 0.0)
        }
        viewModel.getTotalTakenAmount().observe(this) { amount ->
            tvTotalTaken.text = currencyFormat.format(amount ?: 0.0)
        }
    }
    private fun loadLoans() {
        viewModel.getAllLoans().observe(this) { loans ->
            adapter.updateLoans(loans)
        }
    }
    private fun loadLoansGiven() {
        viewModel.getLoansGiven().observe(this) { loans ->
            adapter.updateLoans(loans)
        }
    }
    private fun loadLoansTaken() {
        viewModel.getLoansTaken().observe(this) { loans ->
            adapter.updateLoans(loans)
        }
    }
    private fun showAddLoanDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_loan, null)
        val etPersonName = dialogView.findViewById<EditText>(R.id.etPersonName)
        val etAmount = dialogView.findViewById<EditText>(R.id.etAmount)
        val rgType = dialogView.findViewById<RadioGroup>(R.id.rgType)
        val etInterestRate = dialogView.findViewById<EditText>(R.id.etInterestRate)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val tvLoanDate = dialogView.findViewById<TextView>(R.id.tvLoanDate)
        val tvDueDate = dialogView.findViewById<TextView>(R.id.tvDueDate)
        var loanDate = System.currentTimeMillis()
        var dueDate = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000) // 30 days later
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        tvLoanDate.text = dateFormat.format(Date(loanDate))
        tvDueDate.text = dateFormat.format(Date(dueDate))
        tvLoanDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = loanDate
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    loanDate = calendar.timeInMillis
                    tvLoanDate.text = dateFormat.format(Date(loanDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        tvDueDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = dueDate
            DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    dueDate = calendar.timeInMillis
                    tvDueDate.text = dateFormat.format(Date(dueDate))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
        AlertDialog.Builder(this)
            .setTitle("Add New Loan")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val personName = etPersonName.text.toString().trim()
                val amountStr = etAmount.text.toString().trim()
                val description = etDescription.text.toString().trim()
                val interestRateStr = etInterestRate.text.toString().trim()
                if (personName.isEmpty() || amountStr.isEmpty()) {
                    Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                val interestRate = interestRateStr.toDoubleOrNull() ?: 0.0
                val type = if (rgType.checkedRadioButtonId == R.id.rbGiven) "GIVEN" else "TAKEN"
                val loan = Loan(
                    userId = viewModel.getCurrentUserId(),
                    personName = personName,
                    amount = amount,
                    type = type,
                    date = loanDate,
                    dueDate = dueDate,
                    interestRate = interestRate,
                    status = "ACTIVE",
                    description = description
                )
                viewModel.insertLoan(loan) {
                    Toast.makeText(this, "Loan added successfully!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun showAddPaymentDialog(loan: Loan) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_payment, null)
        val etPaymentAmount = dialogView.findViewById<EditText>(R.id.etPaymentAmount)
        val etPaymentNote = dialogView.findViewById<EditText>(R.id.etPaymentNote)
        val tvRemainingAmount = dialogView.findViewById<TextView>(R.id.tvRemainingAmount)
        val remaining = loan.amount - loan.paidAmount
        tvRemainingAmount.text = "Remaining: ${currencyFormat.format(remaining)}"
        AlertDialog.Builder(this)
            .setTitle("Add Payment")
            .setView(dialogView)
            .setPositiveButton("Add") { dialog, _ ->
                val amountStr = etPaymentAmount.text.toString().trim()
                val note = etPaymentNote.text.toString().trim()
                if (amountStr.isEmpty()) {
                    Toast.makeText(this, "Please enter payment amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                if (amount > remaining) {
                    Toast.makeText(this, "Payment amount cannot exceed remaining amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                viewModel.addPayment(loan.id, amount, note) {
                    Toast.makeText(this, "Payment added successfully!", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
    private fun showLoanDetailsDialog(loan: Loan) {
        val message = buildString {
            append("Person: ${loan.personName}\n")
            append("Type: ${if (loan.type == "GIVEN") "Money Lent" else "Money Borrowed"}\n")
            append("Total Amount: ${currencyFormat.format(loan.amount)}\n")
            append("Paid Amount: ${currencyFormat.format(loan.paidAmount)}\n")
            append("Remaining: ${currencyFormat.format(loan.amount - loan.paidAmount)}\n")
            append("Interest Rate: ${loan.interestRate}%\n")
            append("Status: ${loan.status}\n")
            if (loan.description.isNotEmpty()) {
                append("Description: ${loan.description}\n")
            }
        }
        AlertDialog.Builder(this)
            .setTitle("Loan Details")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .setNeutralButton("Delete") { dialog, _ ->
                showDeleteConfirmation(loan)
                dialog.dismiss()
            }
            .show()
    }
    private fun showDeleteConfirmation(loan: Loan) {
        AlertDialog.Builder(this)
            .setTitle("Delete Loan")
            .setMessage("Are you sure you want to delete this loan?")
            .setPositiveButton("Delete") { dialog, _ ->
                viewModel.deleteLoan(loan)
                Toast.makeText(this, "Loan deleted", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}
