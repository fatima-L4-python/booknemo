package com.example.bookstore

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.adapters.BookAdapter
import com.example.bookstore.lists.BookList
import com.example.bookstore.models.Book
import kotlinx.coroutines.launch
import android.content.Intent
import android.widget.ImageButton
import android.widget.ImageView
import androidx.core.content.ContextCompat

class BookListActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BookAdapter
    private lateinit var bookList: BookList
    private lateinit var userPrefs: UserPrefs

    // Navigation views
    private lateinit var navHome: ImageView
    private lateinit var navList: ImageView
    private lateinit var navSettings: ImageView
    private lateinit var btnBack: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_list)

        // Initialize views
        initializeViews()

        // Setup navigation
        setupNavigationListeners()

        // Set active navigation state (list is active)
        setActiveNavigation("list")

        // Setup RecyclerView
        setupRecyclerView()

        // Setup back button
        btnBack.setOnClickListener {
            finish()
        }
    }

    private fun initializeViews() {
        recyclerView = findViewById(R.id.recycler_view)
        navHome = findViewById(R.id.nav_home)
        navList = findViewById(R.id.nav_list)
        navSettings = findViewById(R.id.nav_settings)
        btnBack = findViewById(R.id.btnBack)

        userPrefs = UserPrefs(this)
    }

    private fun setupNavigationListeners() {
        navHome.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        navList.setOnClickListener {
            // Already on list page, do nothing or refresh
            // You could implement refresh functionality here if needed
        }

        navSettings.setOnClickListener {
            // Navigate to settings activity if you have one
            // For now, just update the visual state
             startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)

        val books = listOf(
            Book(
                "Pride and Prejudice",
                "Jane Austen",
                "romance",
                R.drawable.book__1,
                "https://giove.isti.cnr.it/demo/eread/Libri/joy/Pride.pdf",
                432
            ),

            Book(
                "Murder on the Orient Express",
                "Agatha Christie",
                "mystery",
                R.drawable.book_2,
                "https://detective.gumer.info/anto/christie_8_2.pdf",
                256
            ),

            Book(
                "The Fellowship of the Ring",
                "J.R.R. Tolkien",
                "fantasy",
                R.drawable.book_3,
                "https://s3.amazonaws.com/scschoolfiles/112/j-r-r-tolkien-lord-of-the-rings-01-the-fellowship-of-the-ring-retail-pdf.pdf",
                423
            ),

            Book(
                "Foundation",
                "Isaac Asimov",
                "scifi",
                R.drawable.book_4,
                "https://s3.us-west-1.wasabisys.com/luminist/EB/A/Asimov%20-%20Foundation.pdf",
                296
            ),

            Book(
                "Becoming",
                "Michelle Obama",
                "nonfiction",
                R.drawable.book_5,
                "https://ia801707.us.archive.org/34/items/becomingmichelleobama_201901/Becoming%20-%20Michelle%20Obama.pdf",
                448
            ),

            Book(
                "The Hitchhiker's Guide to the Galaxy",
                "Indie Author",
                "comedy",
                R.drawable.book_6,
                "https://myaccount.inspiruseducation.com/wp-content/uploads/2022/02/The-Hitchhikers-Guide-to-the-Galaxy-Douglas-Adams.pdf",
                224
            ),

            Book(
                "The Time Machine",
                "Indie Author",
                "scifi",
                R.drawable.book_7,
                "https://www.fourmilab.ch/etexts/www/wells/timemach/timemach.pdf",
                84
            ),

            Book(
                "Emma",
                "Jane Austen",
                "romance",
                R.drawable.book_8,
                "https://zabanafzar.ir/files/Dominoes/11-Emma.pdf",
                474
            ),

            Book(
                "The Mysterious Affair at Styles",
                "Agatha Christie",
                "mystery",
                R.drawable.book_9,
                "https://cdn.bookey.app/files/pdf/book/en/the-mysterious-affair-at-styles.pdf",
                212
            ),

            Book(
                "The Hobbit",
                "J.R.R. Tolkien",
                "fantasy",
                R.drawable.book_10,
                "https://rsd2-alert-durden-reading-room.weebly.com/uploads/6/7/1/6/6716949/the_hobbit_tolkien.pdf",
                310
            )
        )

        bookList = BookList(books)

        // Collect user preferences and filter books
        lifecycleScope.launch {
            userPrefs.setFirstLoginComplete();

            userPrefs.getQuestionAnswersSummary().collect { preferences ->
                Log.d("User Preferences", "Preferences: $preferences")
                bookList.filterBooks(preferences)
                val filteredBooks = bookList.getFilteredBooks()

                Log.d("FilteredBooks", "Filtered Books: $filteredBooks")
                adapter = BookAdapter(filteredBooks, this@BookListActivity)
                recyclerView.adapter = adapter
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
}