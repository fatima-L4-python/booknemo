package com.example.bookstore.models

data class Book(
    val title: String,
    val author: String,
    val genre: String,
    val imageResId: Int,
    val pdfLink: String,
    val pages: Int
)