package com.example.bookstore

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var userPrefs: UserPrefs
    private lateinit var navHome: ImageView
    private lateinit var navList: ImageView
    private lateinit var navSettings: ImageView
    private lateinit var btn_help: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize UserPrefs
        userPrefs = UserPrefs(this)

        // Initialize navigation views
        initializeViews()

        // Set up navigation listeners
        setupNavigationListeners()

        // Set initial navigation state (home is active)
        setActiveNavigation("home")
    }

    private fun initializeViews() {
        navHome = findViewById(R.id.nav_home)
        navList = findViewById(R.id.nav_list)
        navSettings = findViewById(R.id.nav_settings)
        btn_help = findViewById(R.id.btn_help)
    }

    private fun setupNavigationListeners() {
        navHome.setOnClickListener {
            // Already on home, just update UI
            setActiveNavigation("home")
        }

        navList.setOnClickListener {
            handleListNavigation()
        }

        navSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        btn_help.setOnClickListener {
            handleListNavigation()
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
                    startActivity(Intent(this@MainActivity, BookListActivity::class.java))
                } else {
                    // Questions not answered, go to QuestionActivity
                    setActiveNavigation("list")
                    startActivity(Intent(this@MainActivity, QuestionActivity::class.java))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Fallback: go to QuestionActivity if there's an error
                setActiveNavigation("list")
                startActivity(Intent(this@MainActivity, QuestionActivity::class.java))
            }
        }
    }

    private fun handleSettingsNavigation() {
        lifecycleScope.launch {
            try {
                setActiveNavigation("settings")
                startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                setActiveNavigation("home")
                startActivity(Intent(this@MainActivity, MainActivity::class.java))
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
        // Reset to home active state when returning to MainActivity
        setActiveNavigation("home")
    }
}