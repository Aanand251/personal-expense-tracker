package com.example.personalexpensetracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ExpenseAdapter(private var expenses: List<Expense>) :
    RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    fun updateList(newList: List<Expense>) {
        expenses = newList
        notifyDataSetChanged()
    }

    inner class ExpenseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title: TextView = view.findViewById(R.id.tvTitle)
            val amount: TextView = view.findViewById(R.id.tvAmount)
            val date: TextView = view.findViewById(R.id.tvDate)

        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.title.text = expense.category
        holder.amount.text = "₹${expense.amount}"
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        holder.date.text = formatter.format(Date(expense.date))
    }


    override fun getItemCount(): Int = expenses.size

    }