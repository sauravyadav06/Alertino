package com.example.mqttalert

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.textfield.TextInputEditText

class LoginActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PREF_NAME = "MQTTAlertPrefs"
    private val KEY_IS_LOGGED_IN = "isLoggedIn"
    private val KEY_USERNAME = "USERNAME" // <-- New key for username

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE)

        // Check if user is already logged in
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            // Skip login and go directly to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish() // Close LoginActivity
            return
        }

        // Set the login layout
        setContentView(R.layout.activity_login)

        // Find views
        val usernameEditText = findViewById<TextInputEditText>(R.id.usernameEditText)
        val deviceTypeEditText = findViewById<TextInputEditText>(R.id.deviceTypeEditText)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // Handle login button click
        loginButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val deviceType = deviceTypeEditText.text.toString().trim()

            if (username.isNotEmpty() && deviceType.isNotEmpty()) {
                // Save login state and username
                with(sharedPreferences.edit()) {
                    putBoolean(KEY_IS_LOGGED_IN, true)
                    putString(KEY_USERNAME, username) // Save username
                    apply()
                }

                // Start MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Close LoginActivity
            } else {
                usernameEditText.error = if (username.isEmpty()) "Username required" else null
                deviceTypeEditText.error = if (deviceType.isEmpty()) "Device type required" else null
            }
        }
    }
}
