package com.example.personalexpensetracker
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
class ReceiptScannerActivity : AppCompatActivity() {
    private lateinit var viewModel: ExpenseViewModel
    private lateinit var ivReceiptPreview: ImageView
    private lateinit var imagePreviewCard: MaterialCardView
    private lateinit var extractedDataCard: MaterialCardView
    private lateinit var actionButtons: View
    private lateinit var etAmount: EditText
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDescription: EditText
    private var currentPhotoPath: String? = null
    private var capturedBitmap: Bitmap? = null
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission required", Toast.LENGTH_SHORT).show()
        }
    }
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && currentPhotoPath != null) {
            displayImage(currentPhotoPath!!)
        }
    }
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                capturedBitmap = BitmapFactory.decodeStream(inputStream)
                displayImageFromBitmap(capturedBitmap!!)
            } catch (e: Exception) {
                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receipt_scanner)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
        viewModel = ViewModelProvider(this)[ExpenseViewModel::class.java]
        ivReceiptPreview = findViewById(R.id.ivReceiptPreview)
        imagePreviewCard = findViewById(R.id.imagePreviewCard)
        extractedDataCard = findViewById(R.id.extractedDataCard)
        actionButtons = findViewById(R.id.actionButtons)
        etAmount = findViewById(R.id.etAmount)
        actvCategory = findViewById(R.id.actvCategory)
        etDescription = findViewById(R.id.etDescription)
        val categories = arrayOf(
            "Food", "Transport", "Shopping", "Entertainment",
            "Bills", "Healthcare", "Education", "Other"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        actvCategory.setAdapter(adapter)
        findViewById<MaterialButton>(R.id.btnTakePhoto).setOnClickListener {
            checkCameraPermissionAndTakePhoto()
        }
        findViewById<MaterialButton>(R.id.btnSelectFromGallery).setOnClickListener {
            pickImageLauncher.launch("image/*")
        }
        findViewById<MaterialButton>(R.id.btnRetake).setOnClickListener {
            resetUI()
        }
        findViewById<MaterialButton>(R.id.btnScan).setOnClickListener {
            scanReceipt()
        }
        findViewById<MaterialButton>(R.id.btnSaveExpense).setOnClickListener {
            saveExpense()
        }
    }
    private fun checkCameraPermissionAndTakePhoto() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                photoFile
            )
            takePictureLauncher.launch(photoURI)
        } catch (e: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
        }
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(null)
        return File.createTempFile("RECEIPT_${timeStamp}_", ".jpg", storageDir).apply {
            currentPhotoPath = absolutePath
        }
    }
    private fun displayImage(imagePath: String) {
        capturedBitmap = BitmapFactory.decodeFile(imagePath)
        displayImageFromBitmap(capturedBitmap!!)
    }
    private fun displayImageFromBitmap(bitmap: Bitmap) {
        ivReceiptPreview.setImageBitmap(bitmap)
        imagePreviewCard.visibility = View.VISIBLE
        actionButtons.visibility = View.GONE
    }
    private fun scanReceipt() {
        if (capturedBitmap == null) {
            Toast.makeText(this, "No image to scan", Toast.LENGTH_SHORT).show()
            return
        }
        Toast.makeText(this, "Scanning receipt...", Toast.LENGTH_SHORT).show()
        val image = InputImage.fromBitmap(capturedBitmap!!, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val extractedText = visionText.text
                val amount = extractAmountFromText(extractedText)
                if (amount > 0) {
                    etAmount.setText(amount.toString())
                    extractedDataCard.visibility = View.VISIBLE
                    Toast.makeText(this, "Amount extracted: â‚¹$amount", Toast.LENGTH_LONG).show()
                } else {
                    extractedDataCard.visibility = View.VISIBLE
                    Toast.makeText(this, "Could not extract amount. Please enter manually.", Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Scan failed: ${e.message}", Toast.LENGTH_SHORT).show()
                extractedDataCard.visibility = View.VISIBLE
            }
    }
    private fun extractAmountFromText(text: String): Double {
        val patterns = listOf(
            Regex("""â‚¹\s*(\d+\.?\d*)"""),
            Regex("""Rs\.?\s*(\d+\.?\d*)"""),
            Regex("""Total\s*:?\s*â‚¹?\s*(\d+\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""Amount\s*:?\s*â‚¹?\s*(\d+\.?\d*)""", RegexOption.IGNORE_CASE),
            Regex("""(\d+\.\d{2})""") // Look for decimal amounts like 123.45
        )
        for (pattern in patterns) {
            val match = pattern.find(text)
            if (match != null && match.groupValues.size > 1) {
                val amountStr = match.groupValues[1]
                return amountStr.toDoubleOrNull() ?: 0.0
            }
        }
        val numbers = Regex("""\d+\.?\d*""").findAll(text).map { it.value.toDoubleOrNull() ?: 0.0 }.toList()
        return numbers.maxOrNull() ?: 0.0
    }
    private fun saveExpense() {
        val amountStr = etAmount.text.toString().trim()
        val category = actvCategory.text.toString().trim()
        val description = etDescription.text.toString().trim()
        if (amountStr.isEmpty() || category.isEmpty()) {
            Toast.makeText(this, "Please fill amount and category", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        if (amount <= 0) {
            Toast.makeText(this, "Please enter valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        val expense = Expense(
            userId = viewModel.getCurrentUserId(),
            title = if (description.isNotEmpty()) description else "Receipt - $category",
            amount = amount,
            category = category,
            name = if (description.isNotEmpty()) description else "Receipt - $category",
            date = System.currentTimeMillis(),
            isFixed = false
        )
        viewModel.insert(expense)
        Toast.makeText(this, "Expense saved successfully! â‚¹$amount", Toast.LENGTH_LONG).show()
        finish()
    }
    private fun resetUI() {
        capturedBitmap = null
        currentPhotoPath = null
        imagePreviewCard.visibility = View.GONE
        extractedDataCard.visibility = View.GONE
        actionButtons.visibility = View.VISIBLE
    }
}
