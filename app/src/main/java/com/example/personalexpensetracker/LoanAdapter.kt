package com.example.personalexpensetracker
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class LoanAdapter(
    private var loans: List<Loan>,
    private val onAddPaymentClick: (Loan) -> Unit,
    private val onDetailsClick: (Loan) -> Unit
) : RecyclerView.Adapter<LoanAdapter.LoanViewHolder>() {

    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "IN"))

    class LoanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvPersonName: TextView = view.findViewById(R.id.tvPersonName)
        val tvLoanType: TextView = view.findViewById(R.id.tvLoanType)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvRemaining: TextView = view.findViewById(R.id.tvRemaining)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvDueDate: TextView = view.findViewById(R.id.tvDueDate)
        val btnAddPayment: MaterialButton = view.findViewById(R.id.btnAddPayment)
        val btnDetails: MaterialButton = view.findViewById(R.id.btnDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoanViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_loan, parent, false)
        return LoanViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoanViewHolder, position: Int) {
        val loan = loans[position]
        val remaining = loan.amount - loan.paidAmount

        holder.tvPersonName.text = loan.personName
        holder.tvLoanType.text = if (loan.type == "GIVEN") " Money Lent" else " Money Borrowed"
        holder.tvLoanType.setTextColor(
            if (loan.type == "GIVEN") Color.parseColor("#4CAF50") 
            else Color.parseColor("#F44336")
        )

        holder.tvStatus.text = loan.status.replace("_", " ")
        when (loan.status) {
            "ACTIVE" -> {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#4CAF50"))
            }
            "PARTIALLY_PAID" -> {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#FF9800"))
            }
            "SETTLED" -> {
                holder.tvStatus.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }
        }

        holder.tvAmount.text = currencyFormat.format(loan.amount)
        holder.tvRemaining.text = currencyFormat.format(remaining)
        holder.tvRemaining.setTextColor(
            if (remaining > 0) Color.parseColor("#F44336") 
            else Color.parseColor("#4CAF50")
        )

        val progress = ((loan.paidAmount / loan.amount) * 100).toInt()
        holder.progressBar.progress = progress

        holder.tvDate.text = " ${dateFormat.format(Date(loan.date))}"
        holder.tvDueDate.text = " Due: ${dateFormat.format(Date(loan.dueDate))}"

        if (loan.dueDate < System.currentTimeMillis() && loan.status != "SETTLED") {
            holder.tvDueDate.setTextColor(Color.parseColor("#D32F2F"))
            holder.tvDueDate.text = " OVERDUE: ${dateFormat.format(Date(loan.dueDate))}"
        } else {
            holder.tvDueDate.setTextColor(Color.parseColor("#757575"))
        }

        holder.btnAddPayment.setOnClickListener { onAddPaymentClick(loan) }
        holder.btnDetails.setOnClickListener { onDetailsClick(loan) }

        holder.btnAddPayment.visibility = if (loan.status == "SETTLED") View.GONE else View.VISIBLE
    }

    override fun getItemCount() = loans.size

    fun updateLoans(newLoans: List<Loan>) {
        loans = newLoans
        notifyDataSetChanged()
    }
}
