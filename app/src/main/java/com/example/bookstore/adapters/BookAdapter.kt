package com.example.bookstore.adapters

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bookstore.models.Book
import android.widget.TextView
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import com.example.bookstore.PdfViewerActivity
import com.example.bookstore.R

class BookAdapter(private var books: List<Book>, private val context: Context) : RecyclerView.Adapter<BookAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.book_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = books[position]
        holder.bind(book)

        // Set click listener to open PDF
        holder.itemView.setOnClickListener {
            openPdf(book.pdfLink, book.title)
        }
    }

    override fun getItemCount(): Int {
        return books.size
    }

    fun updateBooks(newBooks: List<Book>) {
        books = newBooks
        notifyDataSetChanged()
    }

    private fun openPdf(pdfLink: String?, bookTitle: String = "Book") {
        // Check if PDF link is valid
        if (pdfLink.isNullOrEmpty()) {
            Toast.makeText(context, "PDF not available for this book", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            // Option 1: Use custom PdfViewerActivity
            val intent = Intent(context, PdfViewerActivity::class.java).apply {
                putExtra("PDF_LINK", pdfLink)
                putExtra("BOOK_TITLE", bookTitle)
            }
            context.startActivity(intent)

            // Option 2: Direct browser intent (uncomment if PdfViewerActivity fails)
            /*
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfLink))
            context.startActivity(browserIntent)
            */

        } catch (e: Exception) {
            Toast.makeText(context, "Unable to open PDF", Toast.LENGTH_SHORT).show()
            e.printStackTrace()

            // Fallback: try browser
            try {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(pdfLink))
                context.startActivity(browserIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, "No app available to open PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.title_text_view)
        private val authorTextView: TextView = itemView.findViewById(R.id.author_text_view)
        private val genreTextView: TextView = itemView.findViewById(R.id.genre_text_view)
        private val pagesTextView: TextView = itemView.findViewById(R.id.pages_text_view)
        private val imageView: ImageView = itemView.findViewById(R.id.image_view)

        fun bind(book: Book) {
            titleTextView.text = book.title
            authorTextView.text = book.author
            genreTextView.text = book.genre
            pagesTextView.text = "${book.pages} pages"
            imageView.setImageResource(book.imageResId)
        }
    }
}
