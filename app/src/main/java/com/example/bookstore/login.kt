package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var cbRemember: CheckBox
    private lateinit var btnLogin: Button
    private lateinit var tvSignup: TextView
    private lateinit var userPrefs: UserPrefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Initialize views
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        cbRemember = findViewById(R.id.cbRemember)
        btnLogin = findViewById(R.id.btnLogin)
        tvSignup = findViewById(R.id.tvSignup) // Make sure your TextView has this ID

        userPrefs = UserPrefs(this)

        // Check saved credentials
        lifecycleScope.launch {
            if (userPrefs.rememberMe.first()) {
                etEmail.setText(userPrefs.savedEmail.first())
                etPassword.setText(userPrefs.savedPassword.first())
                cbRemember.isChecked = true
            }
        }

        // Login button click
        btnLogin.setOnClickListener {
            val inputEmail = etEmail.text.toString()
            val inputPassword = etPassword.text.toString()

            if (inputEmail.isEmpty() || inputPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (inputEmail.isNotEmpty() && inputPassword.isNotEmpty()) {
                lifecycleScope.launch {
                    userPrefs.resetUserPreferences()
                    val savedEmail = userPrefs.userEmailFlow.first()
                    val savedPassword = userPrefs.userPasswordFlow.first()
                    val isFirstLogin = userPrefs.isFirstLogin.first()

                    if (inputEmail == savedEmail && inputPassword == savedPassword){
                        // Save remember state
                        userPrefs.saveUser(
                            name = userPrefs.userNameFlow.first() ?: "",
                            email = inputEmail,
                            password = inputPassword,
                            rememberMe = cbRemember.isChecked
                        )

                        Toast.makeText(this@LoginActivity, "Login successful", Toast.LENGTH_SHORT).show()
                        if(isFirstLogin){
                            QuestionActivity.start(this@LoginActivity)
                            finish()
                        } else {
                            startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                            finish()
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                    // Proceed to main activity
                }
            }
        }

        // Signup text click
        tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }
}