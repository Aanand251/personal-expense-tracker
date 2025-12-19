package com.example.personalexpensetracker
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.auth.FirebaseAuth
class AnalyticsFragment : Fragment() {
    private lateinit var pieChart: PieChart
    private val expenseViewModel: ExpenseViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pieChart = view.findViewById(R.id.pieChart)
        expenseViewModel.expensesLiveData.observe(viewLifecycleOwner) { expenseList ->
            val grouped = expenseList.groupBy { it.category }
            val entries = grouped.map { (cat, list) ->
                PieEntry(list.sumOf { it.amount.toDouble() }.toFloat(), cat)
            }
            val dataSet = PieDataSet(entries, "Expense Category")
            dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()
            val data = PieData(dataSet)
            pieChart.data = data
            pieChart.invalidate()
        }
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        expenseViewModel.loadUserExpenses(currentUserId)
    }
}
