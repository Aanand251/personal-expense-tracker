package com.example.personalexpensetracker
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth
class SettingsActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefs = getSharedPreferences("user_settings", Context.MODE_PRIVATE)
        setContentView(R.layout.activity_settings)
        auth = FirebaseAuth.getInstance()
        val tvUserEmail = findViewById<TextView>(R.id.tvUserEmail)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val switchTheme = findViewById<SwitchCompat>(R.id.switchTheme)
        tvUserEmail.text = getString(R.string.user_label, auth.currentUser?.email ?: "Guest")
        switchTheme.isChecked = false // Force light mode
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, R.string.logout_msg, Toast.LENGTH_SHORT).show()
            finish()
        }
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchTheme.isChecked = false
                Toast.makeText(this, "Dark mode is currently disabled", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
