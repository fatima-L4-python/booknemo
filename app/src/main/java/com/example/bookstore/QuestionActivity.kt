package com.example.bookstore

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.bookstore.databinding.ActivityQuestionBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

// Data Classes
data class Question(
    val id: String,
    val text: String,
    val options: List<Option>
)

data class Option(
    val id: String,
    val text: String,
    val emoji: String = ""
)
    // Complete Activity Implementation
    class QuestionActivity : AppCompatActivity() {

        private lateinit var binding: ActivityQuestionBinding
        private lateinit var adapter: ArrayAdapter<String>
        private val selectedOptions = mutableSetOf<String>()
        private var currentQuestionIndex = 0
        private lateinit var userPrefs: UserPrefs
        private val questions = listOf(
            Question(
                id = "book_genre",
                text = "If books were a party, what genre would you show up to?",
                options = listOf(
                    Option("fiction", "Fiction — Made-up magic", "📖"),
                    Option("mystery", "Mystery — Whodunnit vibes", "🕵️‍♂️"),
                    Option("fantasy", "Fantasy — Magic and quests", "🐉"),
                    Option("romance", "Romance — Love and drama", "❤️"),
                    Option("scifi", "Sci-Fi — Space and tech", "🚀"),
                    Option("nonfiction", "Non-fiction — Real stories", "📚"),
                    Option("comedy", "Comedy — Light and funny", "😂")
                )
            ),
            Question(
                id = "favorite_author",
                text = "Who's the author you'd invite to your book club (or at least stalk online)?",
                options = listOf(
                    Option("Agatha Christie", "Agatha Christie — Mystery legend", "🔍"),
                    Option("J.R.R. Tolkien", "J.R.R. Tolkien — Epic fantasy master", "🧙‍♂️"),
                    Option("Jane Austen", "Jane Austen — Romance icon", "💕"),
                    Option("Isaac Asimov", "Isaac Asimov — Sci-fi genius", "👽"),
                    Option("Michelle Obama", "Michelle Obama — Inspiring storyteller", "📖"),
                    Option("Indie Author", "Indie Author — Hidden gem vibes", "✨")
                )
            ),
            Question(
                id = "book_length",
                text = "What's your book length vibe?",
                options = listOf(
                    Option("short", "Short — Quick and breezy", "☕"),
                    Option("medium", "Medium — A solid read", "📘"),
                    Option("long", "Long — Settle in and dive deep", "📚")
                )
            )
        )

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            binding = ActivityQuestionBinding.inflate(layoutInflater)
            setContentView(binding.root)

            userPrefs = UserPrefs(this)

            selectedOptions.clear()


            // Get starting question index from intent (if resuming)
            currentQuestionIndex = intent.getIntExtra("question_index", 0)

            setupViews()
            loadCurrentQuestion()
            setupListeners()
        }

        private fun setupViews() {
            // Setup dropdown adapter
            val optionTexts = getCurrentQuestion().options.map { "${it.text} ${it.emoji}" }
            adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, optionTexts)
            binding.autoCompleteDropdown.setAdapter(adapter)

            // Prevent keyboard from showing
            binding.autoCompleteDropdown.inputType = InputType.TYPE_NULL
            binding.autoCompleteDropdown.keyListener = null

            binding.autoCompleteDropdown.isFocusable = false
            binding.autoCompleteDropdown.isClickable = true
            binding.autoCompleteDropdown.isCursorVisible = false

            // Show dropdown when clicked
            binding.autoCompleteDropdown.setOnClickListener {
                binding.autoCompleteDropdown.showDropDown()
            }
        }

        private fun setupListeners() {
            binding.btnBack.setOnClickListener {
                if (currentQuestionIndex > 0) {
                    saveCurrentAnswers()
                    currentQuestionIndex--
                    loadCurrentQuestion()
                } else {
                    finish()
                }
            }

            binding.btnNext.setOnClickListener {
                saveCurrentAnswers()
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    loadCurrentQuestion()
                } else {
                    // All questions completed
                    finishQuestionnaire()
                }
            }

            binding.btnSkip.setOnClickListener {
                if (currentQuestionIndex < questions.size - 1) {
                    currentQuestionIndex++
                    loadCurrentQuestion()
                } else {
                    finishQuestionnaire()
                }
            }

            binding.autoCompleteDropdown.setOnItemClickListener { _, _, position, _ ->
                val selectedOption = getCurrentQuestion().options[position]
                addSelectedOption(selectedOption)
                binding.autoCompleteDropdown.setText("", false)
            }
        }

        private fun loadCurrentQuestion() {
            val question = getCurrentQuestion()
            binding.tvQuestion.text = question.text

            // Update dropdown options
            val optionTexts = question.options.map { "${it.text} ${it.emoji}" }
//            adapter.clear()
//            adapter.addAll(optionTexts)
//            adapter.notifyDataSetChanged()
            val newAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, optionTexts)
            binding.autoCompleteDropdown.setAdapter(newAdapter)

            // Update hint text for current question
            binding.textInputLayout.hint = when(question.id) {
                "book_genre" -> "Select genres"
                "favorite_author" -> "Select author types"
                "book_length" -> "Select length preferences"
                else -> "Select options"
            }

            // Load saved answers for this question
            loadSavedAnswers()
            updateChipGroup()

            // Update skip button visibility (show only on last question if needed)
            binding.btnSkip.visibility = if (currentQuestionIndex == questions.size - 1) View.VISIBLE else View.GONE

            binding.autoCompleteDropdown.setText("", false)
        }

        private fun addSelectedOption(option: Option) {
            if (!selectedOptions.contains(option.id)) {
                selectedOptions.add(option.id)
                updateChipGroup()
            }
        }

        private fun updateChipGroup() {
            binding.chipGroupSelected.removeAllViews()
            val currentQuestion = getCurrentQuestion()

            selectedOptions.forEach { optionId ->
                val option = currentQuestion.options.find { it.id == optionId }
                option?.let {
                    val chip = Chip(this)
                    chip.text = "${it.text} ${it.emoji}"
                    chip.isCloseIconVisible = true
                    chip.setOnCloseIconClickListener {
                        selectedOptions.remove(optionId)
                        updateChipGroup()
                    }
                    binding.chipGroupSelected.addView(chip)
                }
            }
        }

        private fun getCurrentQuestion(): Question = questions[currentQuestionIndex]

        private fun saveCurrentAnswers() {
            val question = getCurrentQuestion()
            lifecycleScope.launch {
                val selectedAnswers = selectedOptions.toSet()
                userPrefs.saveQuestionAnswers(question.id, selectedAnswers)
            }
        }

        private fun loadSavedAnswers() {
            val question = getCurrentQuestion()
            lifecycleScope.launch {
                userPrefs.getQuestionAnswers(question.id).collect { savedAnswers ->
                    selectedOptions.clear()
                    selectedOptions.addAll(savedAnswers)
                    updateChipGroup()
                }
            }
        }

        private fun finishQuestionnaire() {
            // Save current answers before finishing
            saveCurrentAnswers()

            // You can navigate to the next screen or show completion
            Toast.makeText(this, "All questions completed! 📚", Toast.LENGTH_SHORT).show()

            // Example: Navigate to main app or dashboard
             startActivity(Intent(this, BookListActivity::class.java))
            finish()
        }

        // Helper method to start questionnaire from any activity
        companion object {
            fun start(context: Context, startFromQuestion: Int = 0) {
                val intent = Intent(context, QuestionActivity::class.java)
                intent.putExtra("question_index", startFromQuestion)
                context.startActivity(intent)
            }
        }
    }
