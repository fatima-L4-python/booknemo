package com.example.bookstore

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class PdfViewerActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pdf_viewer)

        webView = findViewById(R.id.webView)
        progressBar = findViewById(R.id.progressBar)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener{
            onBackPressed()
        }

        val pdfLink = intent.getStringExtra("PDF_LINK")
        val bookTitle = intent.getStringExtra("BOOK_TITLE") ?: "PDF Viewer"

        // Set title
        supportActionBar?.title = bookTitle

        if (pdfLink.isNullOrEmpty()) {
            Toast.makeText(this, "No PDF link provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupWebView()
        loadPdf(pdfLink)
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                progressBar.visibility = ProgressBar.GONE
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                progressBar.visibility = ProgressBar.GONE

                Toast.makeText(
                    this@PdfViewerActivity,
                    "Failed to load PDF. Opening in browser...",
                    Toast.LENGTH_LONG
                ).show()

                // Fallback: Open in external browser
                openInBrowser(request?.url.toString())
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = ProgressBar.GONE
                }
            }
        }
    }

    private fun loadPdf(pdfLink: String) {
        try {
            // Method 1: Try Google Docs Viewer
            val googleDocsUrl = "https://docs.google.com/viewer?url=${Uri.encode(pdfLink)}"
            webView.loadUrl(googleDocsUrl)

        } catch (e: Exception) {
            Toast.makeText(this, "Error loading PDF: ${e.message}", Toast.LENGTH_SHORT).show()
            openInBrowser(pdfLink)
        }
    }

    private fun openInBrowser(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "Unable to open PDF", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}