package com.example.personalexpensetracker
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.auth.FirebaseAuth
object ExpenseManager {
    private const val CHANNEL_ID = "expense_alerts"
    private const val NOTIFICATION_ID = 1001
    fun canAffordExpense(
        context: Context,
        expenseAmount: Double,
        currentUserId: String,
        totalExpenses: Double,
        totalSavings: Double
    ): Pair<Boolean, String> {
        val monthlyIncome = PrefsHelper.getMonthlyIncome(context, currentUserId)
        val fixedExpense = PrefsHelper.getFixedExpense(context, currentUserId)
        val newTotalExpenses = totalExpenses + fixedExpense + expenseAmount
        val availableFunds = monthlyIncome + totalSavings
        if (monthlyIncome == 0.0 && totalSavings == 0.0) {
            return Pair(false, "âš ï¸ Cannot add expense!\n\nYou have no monthly income or savings set. Please add income or savings first.")
        }
        if (newTotalExpenses > availableFunds) {
            val shortfall = newTotalExpenses - availableFunds
            return Pair(false, "âš ï¸ Insufficient funds!\n\nExpense: â‚¹$expenseAmount\nAvailable: â‚¹${availableFunds.toInt()}\nShortfall: â‚¹${shortfall.toInt()}\n\nPlease reduce expense or add more income/savings.")
        }
        return Pair(true, "")
    }
    fun processExpenseAndUpdateSavings(
        context: Context,
        expenseAmount: Double,
        currentUserId: String,
        totalExpenses: Double,
        totalSavings: Double,
        savingsViewModel: SavingsViewModel
    ): String {
        val monthlyIncome = PrefsHelper.getMonthlyIncome(context, currentUserId)
        val fixedExpense = PrefsHelper.getFixedExpense(context, currentUserId)
        val totalExpensesWithFixed = totalExpenses + fixedExpense + expenseAmount
        if (totalExpensesWithFixed > monthlyIncome) {
            val excessAmount = totalExpensesWithFixed - monthlyIncome
            if (totalSavings >= excessAmount) {
                val savingToDeduct = Savings(
                    userId = currentUserId,
                    amount = -excessAmount, // Negative to deduct
                    note = "Auto-deducted: Expenses exceeded income",
                    date = System.currentTimeMillis()
                )
                savingsViewModel.insert(savingToDeduct)
                showNotification(
                    context,
                    "âš ï¸ Expenses Exceed Income!",
                    "â‚¹${excessAmount.toInt()} deducted from your savings. Current expenses: â‚¹${totalExpensesWithFixed.toInt()}, Income: â‚¹${monthlyIncome.toInt()}"
                )
                return "âš ï¸ Warning: Expenses (â‚¹${totalExpensesWithFixed.toInt()}) exceeded income (â‚¹${monthlyIncome.toInt()}). â‚¹${excessAmount.toInt()} deducted from savings."
            }
        }
        else if (totalExpensesWithFixed < monthlyIncome) {
            val remainingIncome = monthlyIncome - totalExpensesWithFixed
            val savingToAdd = Savings(
                userId = currentUserId,
                amount = remainingIncome,
                note = "Auto-saved: Remaining income",
                date = System.currentTimeMillis()
            )
            savingsViewModel.insert(savingToAdd)
            showNotification(
                context,
                "âœ… Great Job!",
                "â‚¹${remainingIncome.toInt()} saved! Expenses: â‚¹${totalExpensesWithFixed.toInt()}, Income: â‚¹${monthlyIncome.toInt()}"
            )
            return "âœ… Good news! â‚¹${remainingIncome.toInt()} automatically added to your savings!"
        }
        return ""
    }
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Expense Alerts"
            val descriptionText = "Notifications for expense tracking and budget alerts"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    private fun showNotification(context: Context, title: String, message: String) {
        try {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
            val notificationManager = NotificationManagerCompat.from(context)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                ) {
                    notificationManager.notify(NOTIFICATION_ID, builder.build())
                }
            } else {
                notificationManager.notify(NOTIFICATION_ID, builder.build())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
