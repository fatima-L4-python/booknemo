package com.example.bookstore.lists

import com.example.bookstore.UserPrefs
import com.example.bookstore.models.Book

class BookList(private val books: List<Book>) {
    private var filteredBooks: List<Book> = books
    fun filterBooks(preferences: Map<String, Set<String>>) {
        val selectedGenres = preferences["book_genre"] ?: emptySet()
        val selectedAuthors = preferences["favorite_author"] ?: emptySet()
        val selectedLengths = preferences["book_length"] ?: emptySet()
        filteredBooks = books.filter { book ->
            (selectedGenres.isEmpty() || book.genre in selectedGenres) &&
                    (selectedAuthors.isEmpty() || book.author in selectedAuthors) &&
                    (selectedLengths.isEmpty() || when {
                        selectedLengths.contains("short") && book.pages < 200 -> true
                        selectedLengths.contains("medium") && book.pages in 200..500 -> true
                        selectedLengths.contains("long") && book.pages > 500 -> true
                        else -> false
                    })
        }
    }
    fun getFilteredBooks(): List<Book> {
        return filteredBooks
    }
    fun resetFilter() {
        filteredBooks = books
    }
}