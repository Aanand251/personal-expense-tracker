package com.example.personalexpensetracker
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
class AddExpenseActivity : AppCompatActivity() {
    private lateinit var etAmount: TextInputEditText
    private lateinit var etCategory: AutoCompleteTextView
    private lateinit var etNote: TextInputEditText
    private val voiceInputLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val spokenText = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            spokenText?.let {
                if (it.isNotEmpty()) {
                    processVoiceInput(it[0])
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_expense)
        etAmount = findViewById(R.id.etAmount)
        etCategory = findViewById(R.id.etCategory)
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        etNote = findViewById(R.id.etNote)
        val btnSave = findViewById<MaterialButton>(R.id.btnSave)
        val btnVoiceInput = findViewById<MaterialButton>(R.id.btnVoiceInput)
        val categories = listOf("Food", "Transport", "Shopping", "Recharge", "Bills", "Entertainment", "Healthcare", "Education", "Other")
        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        etCategory.setAdapter(categoryAdapter)
        etNote.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus && etNote.text.toString().isNotEmpty()) {
                suggestCategory(etNote.text.toString())
            }
        }
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
        btnVoiceInput.setOnClickListener {
            startVoiceInput()
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
    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something like: Spent 500 on food")
        }
        try {
            voiceInputLauncher.launch(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Voice input not supported on your device", Toast.LENGTH_SHORT).show()
        }
    }
    private fun processVoiceInput(spokenText: String) {
        Toast.makeText(this, "Processing: $spokenText", Toast.LENGTH_SHORT).show()
        val amountPattern = Regex("""(\d+\.?\d*)""")
        val amountMatch = amountPattern.find(spokenText)
        if (amountMatch != null) {
            val amount = amountMatch.value
            etAmount.setText(amount)
        }
        val detectedCategory = detectCategoryFromText(spokenText)
        if (detectedCategory.isNotEmpty()) {
            etCategory.setText(detectedCategory, false)
        }
        etNote.setText(spokenText)
        Toast.makeText(this, "âœ… Voice input processed!", Toast.LENGTH_SHORT).show()
    }
    private fun detectCategoryFromText(text: String): String {
        val lowerText = text.lowercase()
        val categoryKeywords = mapOf(
            "Food" to listOf("food", "lunch", "dinner", "breakfast", "snack", "meal", "restaurant", "cafe", "pizza", "burger", "coffee", "tea", "biryani", "swiggy", "zomato", "eat", "ate"),
            "Transport" to listOf("transport", "taxi", "uber", "ola", "bus", "train", "metro", "petrol", "fuel", "gas", "parking", "toll", "auto", "rickshaw", "bike", "car", "travel"),
            "Shopping" to listOf("shopping", "clothes", "shirt", "shoes", "dress", "pants", "jacket", "buy", "bought", "purchase", "amazon", "flipkart", "myntra", "store", "mall"),
            "Recharge" to listOf("recharge", "mobile", "phone", "prepaid", "data", "internet", "jio", "airtel", "vi", "vodafone", "broadband"),
            "Bills" to listOf("bill", "electricity", "water", "rent", "emi", "loan", "insurance", "subscription", "netflix", "prime", "spotify", "utility"),
            "Entertainment" to listOf("movie", "cinema", "theatre", "show", "concert", "game", "gaming", "party", "club", "pub", "entertainment", "fun"),
            "Healthcare" to listOf("doctor", "hospital", "medicine", "medical", "pharmacy", "health", "clinic", "checkup", "treatment", "covid", "vaccine"),
            "Education" to listOf("book", "course", "class", "tuition", "school", "college", "university", "education", "study", "exam", "fee")
        )
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerText.contains(keyword)) {
                    return category
                }
            }
        }
        return "Other"
    }
    private fun suggestCategory(note: String) {
        if (etCategory.text.toString().isEmpty()) {
            val suggestedCategory = detectCategoryFromText(note)
            if (suggestedCategory != "Other") {
                etCategory.setText(suggestedCategory, false)
                Toast.makeText(this, "ðŸ’¡ Auto-suggested: $suggestedCategory", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
