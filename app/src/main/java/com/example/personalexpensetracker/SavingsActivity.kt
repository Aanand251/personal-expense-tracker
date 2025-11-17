package com.example.personalexpensetracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import java.text.NumberFormat
import java.util.Locale

class SavingsActivity : AppCompatActivity() {

    private lateinit var vm: SavingsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_savings)

        val etAmount = findViewById<EditText>(R.id.etAmount)
        val etNote = findViewById<EditText>(R.id.etNote)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val tvTotalSavings = findViewById<TextView>(R.id.tvTotalSavings)
//        val tvTotalSavingsmain = findViewById<TextView>(R.id.tvTotalSavingsMain)


        vm = ViewModelProvider(this)[SavingsViewModel::class.java]

        btnSave.setOnClickListener {
            val amountStr = etAmount.text.toString().trim()
            val amount = amountStr.toDoubleOrNull() ?: 0.0
            val note = etNote.text?.toString()?.trim() ?: ""

            if (amount <= 0.0) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""
            if (currentUserId.isEmpty()) {
                Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val saving = Savings(
                userId = currentUserId,
                amount = amount,
                note = note,
                date = System.currentTimeMillis()
            )
            vm.insert(saving)

            Toast.makeText(this, "Saving added!", Toast.LENGTH_SHORT).show()
            etAmount.text?.clear()
            etNote.text?.clear()
        }

        vm.getTotalSavings().observe(this) { total ->
            val safeTotal = total ?: 0.0
            val formatted = NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(safeTotal)
            tvTotalSavings.text = getString(R.string.total_savings, formatted)
        }

    }
}
