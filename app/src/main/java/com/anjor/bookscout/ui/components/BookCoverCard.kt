package com.anjor.bookscout.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.anjor.bookscout.R
import com.anjor.bookscout.domain.model.BookSummary

/**
 * A cover-card for the search grid. Shows the book cover (or a placeholder when the API
 * provides no cover / the image fails to load), plus title, authors and publication year.
 */
@Composable
fun BookCoverCard(
    book: BookSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            BookAsyncImage(
                coverUrl = book.coverUrl,
                title = book.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (book.authors.isNotEmpty()) {
                    Text(
                        text = book.authors.joinToString(", "),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                book.firstPublishYear?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

/**
 * Renders the cover image, gracefully handling three cases:
 * 1. A valid URL -> load with Coil (with placeholder + error fallback).
 * 2. A null/blank URL -> show the book placeholder icon immediately.
 * 3. Load failure -> fall back to the placeholder icon.
 */
@Composable
fun BookAsyncImage(
    coverUrl: String?,
    title: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val placeholder = painterResource(id = R.drawable.book_placeholder)

    if (coverUrl.isNullOrBlank()) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.MenuBook,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxSize(0.45f)
            )
        }
    } else {
        AsyncImage(
            model = coverUrl,
            contentDescription = title,
            modifier = modifier,
            contentScale = ContentScale.Crop,
            placeholder = placeholder,
            error = placeholder
        )
    }
}
