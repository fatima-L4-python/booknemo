package com.example.bookstore

// UserPrefs.kt
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPrefs(private val context: Context) {

    companion object {
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val PASSWORD = stringPreferencesKey("password")
        val REMEMBER_ME = booleanPreferencesKey("remember_me")
        val FIRST_LOGIN = booleanPreferencesKey("first_login")
        val DARK_MODE = booleanPreferencesKey("dark_mode")

        val BOOK_GENRE = stringSetPreferencesKey("book_genre")
        val FAVORITE_AUTHOR = stringSetPreferencesKey("favorite_author")
        val BOOK_LENGTH = stringSetPreferencesKey("book_length")
    }

    // Save user credentials
    suspend fun saveUser(email: String, password: String, rememberMe: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[EMAIL] = email
            prefs[PASSWORD] = password
            prefs[REMEMBER_ME] = rememberMe
        }
    }

    // Save user's complete details
    suspend fun saveUser(name: String, email: String, password: String, rememberMe: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[NAME] = name
            prefs[EMAIL] = email
            prefs[PASSWORD] = password
            prefs[REMEMBER_ME] = rememberMe
            prefs[FIRST_LOGIN] = true
        }
    }

    suspend fun saveQuestionAnswers(questionId: String, answers: Set<String>) {
        val key = when (questionId) {
            "book_genre" -> BOOK_GENRE
            "favorite_author" -> FAVORITE_AUTHOR
            "book_length" -> BOOK_LENGTH
            else -> stringSetPreferencesKey(questionId)
        }

        context.userPrefsDataStore.edit { prefs ->
            prefs[key] = answers
        }
    }

    fun getQuestionAnswers(questionId: String): Flow<Set<String>> {
        val key = when (questionId) {
            "book_genre" -> BOOK_GENRE
            "favorite_author" -> FAVORITE_AUTHOR
            "book_length" -> BOOK_LENGTH
            else -> stringSetPreferencesKey(questionId)
        }

        return context.userPrefsDataStore.data.map { prefs ->
            prefs[key] ?: emptySet()
        }
    }

    suspend fun resetUserPreferences() {
        clearQuestionAnswers() // Clear saved question answers // Optionally clear all user data (email, password, etc.)
    }

    // Get saved email
    val savedEmail: Flow<String?> = context.userPrefsDataStore.data
        .map { prefs -> prefs[EMAIL] }

    // Get saved password
    val savedPassword: Flow<String?> = context.userPrefsDataStore.data
        .map { prefs -> prefs[PASSWORD] }

    // Check if remember me is enabled
    val rememberMe: Flow<Boolean> = context.userPrefsDataStore.data
        .map { prefs -> prefs[REMEMBER_ME] ?: false }

    val userEmailFlow: Flow<String?> = context.userPrefsDataStore.data
        .map { it[EMAIL] }

    val userPasswordFlow: Flow<String?> = context.userPrefsDataStore.data
        .map { it[PASSWORD] }

    val userNameFlow: Flow<String?> = context.userPrefsDataStore.data
        .map { it[NAME] }

    val rememberMeFlow: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[REMEMBER_ME] ?: false }

    // Check if this is the first login
    val isFirstLogin: Flow<Boolean> = context.userPrefsDataStore.data
        .map { prefs -> prefs[FIRST_LOGIN] ?: false }

    val isDarkMode: Flow<Boolean> = context.userPrefsDataStore.data
        .map { it[DARK_MODE] ?: false }

    // Get all question answers as flows for easy access
    val bookGenreAnswers: Flow<Set<String>> = context.userPrefsDataStore.data
        .map { it[BOOK_GENRE] ?: emptySet() }

    val favoriteAuthorAnswers: Flow<Set<String>> = context.userPrefsDataStore.data
        .map { it[FAVORITE_AUTHOR] ?: emptySet() }

    val bookLengthAnswers: Flow<Set<String>> = context.userPrefsDataStore.data
        .map { it[BOOK_LENGTH] ?: emptySet() }

    // Helper method to check if all questions are answered
    fun areAllQuestionsAnswered(): Flow<Boolean> = context.userPrefsDataStore.data
        .map { prefs ->
            val genre = prefs[BOOK_GENRE]?.isNotEmpty() ?: false
            val author = prefs[FAVORITE_AUTHOR]?.isNotEmpty() ?: false
            val length = prefs[BOOK_LENGTH]?.isNotEmpty() ?: false
            genre && author && length
        }

    // Clear all question answers
    suspend fun clearQuestionAnswers() {
        context.userPrefsDataStore.edit { prefs ->
            prefs.remove(BOOK_GENRE)
            prefs.remove(FAVORITE_AUTHOR)
            prefs.remove(BOOK_LENGTH)
        }
    }

    // Get formatted summary of all answers
    fun getQuestionAnswersSummary(): Flow<Map<String, Set<String>>> = context.userPrefsDataStore.data
        .map { prefs ->
            mapOf(
                "book_genre" to (prefs[BOOK_GENRE] ?: emptySet()),
                "favorite_author" to (prefs[FAVORITE_AUTHOR] ?: emptySet()),
                "book_length" to (prefs[BOOK_LENGTH] ?: emptySet())
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[DARK_MODE] = enabled
        }
    }

    // Set first login to false (call this after handling first login)
    suspend fun setFirstLoginComplete() {
        context.userPrefsDataStore.edit { prefs ->
            prefs[FIRST_LOGIN] = false
        }
    }

    suspend fun clearUser() {
        context.userPrefsDataStore.edit { it.clear() }
    }


}