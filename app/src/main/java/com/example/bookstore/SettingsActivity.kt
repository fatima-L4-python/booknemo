package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var userPrefs: UserPrefs
    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var switchRememberMe: Switch
    private lateinit var btnViewPreferences: Button
    private lateinit var btnClearPreferences: Button
    private lateinit var btnLogout: Button
    private lateinit var navHome: ImageView
    private lateinit var navList: ImageView
    private lateinit var navSettings: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        userPrefs = UserPrefs(this)
        initViews()
        setupListeners()
        loadUserData()

        // Set up navigation listeners
        setupNavigationListeners()

        // Set initial navigation state (home is active)
        setActiveNavigation("settings")
    }

    private fun initViews() {
        tvUserName = findViewById(R.id.tvUserName)
        tvUserEmail = findViewById(R.id.tvUserEmail)
        switchRememberMe = findViewById(R.id.switchRememberMe)
        btnViewPreferences = findViewById(R.id.btnViewPreferences)
        btnClearPreferences = findViewById(R.id.btnClearPreferences)
        btnLogout = findViewById(R.id.btnLogout)
        navHome = findViewById(R.id.nav_home)
        navList = findViewById(R.id.nav_list)
        navSettings = findViewById(R.id.nav_settings)

        // Set up action bar
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupListeners() {

        switchRememberMe.setOnCheckedChangeListener { _, isChecked ->
            lifecycleScope.launch {
                val email = userPrefs.savedEmail.first() ?: ""
                val password = userPrefs.savedPassword.first() ?: ""
                userPrefs.saveUser(email, password, isChecked)
                Toast.makeText(this@SettingsActivity,
                    if (isChecked) "Remember me enabled" else "Remember me disabled",
                    Toast.LENGTH_SHORT).show()
            }
        }

        btnViewPreferences.setOnClickListener {
            showUserPreferences()
        }

        btnClearPreferences.setOnClickListener {
            showClearPreferencesDialog()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun setupNavigationListeners() {
        navHome.setOnClickListener {
            // Already on settings, just update UI
            startActivity(Intent(this, MainActivity::class.java))
        }

        navList.setOnClickListener {
            handleListNavigation()
        }

        navSettings.setOnClickListener {
            setActiveNavigation("settings")

        }
    }

    private fun loadUserData() {
        lifecycleScope.launch {
            // Load user info
            val name = userPrefs.userNameFlow.first() ?: "Guest"
            val email = userPrefs.userEmailFlow.first() ?: "No email"

            tvUserName.text = name
            tvUserEmail.text = email

            // Load preferences

            switchRememberMe.isChecked = userPrefs.rememberMeFlow.first()
        }
    }

    private fun showUserPreferences() {
        lifecycleScope.launch {
            val preferences = userPrefs.getQuestionAnswersSummary().first()

            val dialogBuilder = AlertDialog.Builder(this@SettingsActivity)
            dialogBuilder.setTitle("Your Reading Preferences")

            val message = buildString {
                append("Book Genres:\n")
                val genres = preferences["book_genre"] ?: emptySet()
                if (genres.isEmpty()) {
                    append("  Not set\n")
                } else {
                    genres.forEach { append("  • $it\n") }
                }

                append("\nFavorite Authors:\n")
                val authors = preferences["favorite_author"] ?: emptySet()
                if (authors.isEmpty()) {
                    append("  Not set\n")
                } else {
                    authors.forEach { append("  • $it\n") }
                }

                append("\nBook Length Preference:\n")
                val lengths = preferences["book_length"] ?: emptySet()
                if (lengths.isEmpty()) {
                    append("  Not set\n")
                } else {
                    lengths.forEach { append("  • $it\n") }
                }
            }

            dialogBuilder.setMessage(message)
            dialogBuilder.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            dialogBuilder.setNeutralButton("Update Preferences") { dialog, _ ->
                // Navigate to preferences/questions activity
                // startActivity(Intent(this@SettingsActivity, QuestionsActivity::class.java))
                dialog.dismiss()
            }

            dialogBuilder.show()
        }
    }

    private fun showClearPreferencesDialog() {
        AlertDialog.Builder(this)
            .setTitle("Clear Reading Preferences")
            .setMessage("Are you sure you want to clear all your reading preferences? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                clearReadingPreferences()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun clearReadingPreferences() {
        lifecycleScope.launch {
            userPrefs.clearQuestionAnswers()
            Toast.makeText(this@SettingsActivity,
                "Reading preferences cleared successfully",
                Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        lifecycleScope.launch {
            Toast.makeText(this@SettingsActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()

            // Navigate to login activity and clear back stack
            val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun handleListNavigation() {
        lifecycleScope.launch {
            try {
                // Check if all questions are answered
                val allAnswered = userPrefs.areAllQuestionsAnswered().first()

                if (allAnswered) {
                    // All questions answered, go to BookListActivity
                    setActiveNavigation("list")
                    startActivity(Intent(this@SettingsActivity, BookListActivity::class.java))
                } else {
                    // Questions not answered, go to QuestionActivity
                    setActiveNavigation("list")
                    startActivity(Intent(this@SettingsActivity, QuestionActivity::class.java))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: go to QuestionActivity if there's an error
                setActiveNavigation("list")
                startActivity(Intent(this@SettingsActivity, QuestionActivity::class.java))
            }
        }
    }

    private fun setActiveNavigation(activeTab: String) {
        // Reset all navigation items to inactive state
        resetNavigationStates()

        when (activeTab) {
            "home" -> {
                navHome.setImageResource(R.drawable.icons8_home_50_white)
                navHome.setBackgroundColor(ContextCompat.getColor(this, R.color.link_active))
            }
            "list" -> {
                navList.setImageResource(R.drawable.icons8_list_50_white)
                navList.setBackgroundColor(ContextCompat.getColor(this, R.color.link_active))
            }
            "settings" -> {
                navSettings.setImageResource(R.drawable.icons8_settings_50_white)
                navSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.link_active))
            }
        }
    }

    private fun resetNavigationStates() {
        // Reset home
        navHome.setImageResource(R.drawable.icons8_home_50)
        navHome.setBackgroundColor(ContextCompat.getColor(this, R.color.background_primary))

        // Reset list
        navList.setImageResource(R.drawable.icons8_list_50)
        navList.setBackgroundColor(ContextCompat.getColor(this, R.color.background_primary))

        // Reset settings
        navSettings.setImageResource(R.drawable.icons8_settings_50)
        navSettings.setBackgroundColor(ContextCompat.getColor(this, R.color.background_primary))
    }

    override fun onResume() {
        super.onResume()
        // Reset to settings active state when returning to SettingsActivity
        setActiveNavigation("settings")
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}