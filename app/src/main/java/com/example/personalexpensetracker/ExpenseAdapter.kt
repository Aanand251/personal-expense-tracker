package com.example.personalexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private var expenses: List<Expense>,
    private val onDeleteClick: ((Expense) -> Unit)? = null
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val btnDelete: ImageView? = try {
            view.findViewById(R.id.btnDelete)
        } catch (e: Exception) {
            null
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]

        holder.tvCategory.text = expense.category
        holder.tvAmount.text = "₹${expense.amount.toInt()}"

        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        holder.tvDate.text = dateFormat.format(Date(expense.date))

        // Handle delete button
        if (onDeleteClick != null && holder.btnDelete != null) {
            holder.btnDelete.visibility = View.VISIBLE
            holder.btnDelete.setOnClickListener {
                val currentPosition = holder.bindingAdapterPosition
                if (currentPosition != RecyclerView.NO_POSITION && currentPosition < expenses.size) {
                    onDeleteClick.invoke(expenses[currentPosition])
                }
            }
        } else {
            holder.btnDelete?.visibility = View.GONE
        }
    }

    override fun getItemCount() = expenses.size

    fun updateExpenses(newExpenses: List<Expense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}