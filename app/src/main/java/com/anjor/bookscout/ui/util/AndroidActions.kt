package com.anjor.bookscout.ui.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.anjor.bookscout.domain.model.BookDetails
import com.anjor.bookscout.domain.model.BookSummary

/** Open a URL in the user's default browser, failing silently if none is available. */
fun openInBrowser(context: Context, url: String?) {
    if (url.isNullOrBlank()) return
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: ActivityNotFoundException) {
        // No browser installed; nothing we can do, just don't crash.
    }
}

/** Share a book via the system share sheet (text + Open Library link). */
fun shareBook(context: Context, book: BookDetails) {
    val text = buildString {
        append(book.title)
        if (book.authors.isNotEmpty()) append(" by ${book.authors.joinToString(", ")}")
        append("\n")
        append(book.openLibraryUrl)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, book.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getText(android.R.string.shareText)))
}

/** Share a search result card (used from the details screen fallback path). */
fun shareBook(context: Context, book: BookSummary) {
    val text = buildString {
        append(book.title)
        if (book.authors.isNotEmpty()) append(" by ${book.authors.joinToString(", ")}")
        append("\n")
        append(book.openLibraryUrl)
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TITLE, book.title)
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, context.getText(android.R.string.shareText)))
}
