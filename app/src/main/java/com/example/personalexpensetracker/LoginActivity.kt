package com.example.personalexpensetracker
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            showAuthError("Login", task.exception)
                        }
                    }
            }
        }
        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show()
                        } else {
                            showAuthError("Register", task.exception)
                        }
                    }
            }
        }
    }
    private fun showAuthError(tag: String, ex: Exception?) {
        val errorCode = (ex as? FirebaseAuthException)?.errorCode ?: "UNKNOWN"
        val msg = when (errorCode) {
            "ERROR_INVALID_EMAIL" -> "The email address is badly formatted."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "This email address is already registered."
            "ERROR_WEAK_PASSWORD" -> "Password is too weak. Use at least 6 characters."
            "ERROR_WRONG_PASSWORD" -> "Incorrect password."
            "ERROR_USER_NOT_FOUND" -> "No account found with this email."
            else -> ex?.localizedMessage ?: "Authentication failed."
        }
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
    }
}
