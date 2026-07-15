package com.anjor.bookscout.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.anjor.bookscout.R
import com.anjor.bookscout.ui.components.BookAsyncImage
import com.anjor.bookscout.ui.components.ErrorState
import com.anjor.bookscout.ui.components.ShimmerBlock
import com.anjor.bookscout.ui.util.openInBrowser
import com.anjor.bookscout.ui.util.shareBook

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    uiState: DetailsUiState,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.book?.title ?: stringResource(R.string.title_details)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        when (uiState.status) {
            DetailsStatus.LOADING -> LoadingDetails(padding)
            DetailsStatus.ERROR -> ErrorState(
                error = uiState.error,
                onRetry = onRetry,
                modifier = Modifier.padding(padding)
            )
            DetailsStatus.SUCCESS -> {
                val book = uiState.book
                if (book != null) {
                    BookDetailsContent(
                        book = book,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    )
                } else {
                    ErrorState(error = uiState.error, onRetry = onRetry, modifier = Modifier.padding(padding))
                }
            }
        }
    }
}

@Composable
private fun LoadingDetails(padding: androidx.compose.foundation.layout.PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp))
        )
        ShimmerBlock(modifier = Modifier.fillMaxWidth(0.8f).height(24.dp))
        ShimmerBlock(modifier = Modifier.fillMaxWidth(0.5f).height(16.dp))
        ShimmerBlock(modifier = Modifier.fillMaxWidth().height(120.dp))
    }
}

@Composable
private fun BookDetailsContent(book: com.anjor.bookscout.domain.model.BookDetails, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Column(
        modifier = modifier.verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Cover
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            BookAsyncImage(
                coverUrl = book.coverUrl,
                title = book.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Title + authors
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = book.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            if (book.authors.isNotEmpty()) {
                Text(
                    text = book.authors.joinToString(", "),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            val yearLabel = book.firstPublishDate ?: book.firstPublishYear?.toString()
            if (!yearLabel.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.first_published, yearLabel),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { openInBrowser(context, book.openLibraryUrl) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.OpenInNew, contentDescription = null)
                Text(
                    text = stringResource(R.string.action_open_ol),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
            Button(
                onClick = { shareBook(context, book) },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.Share, contentDescription = null)
                Text(
                    text = stringResource(R.string.action_share),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Subjects
        if (book.subjects.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.subjects),
                    style = MaterialTheme.typography.titleSmall
                )
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    book.subjects.take(15).forEach { subject ->
                        AssistChip(onClick = { }, label = { Text(subject) })
                    }
                }
            }
        }

        // Description
        if (!book.description.isNullOrBlank()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = stringResource(R.string.description),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = book.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Metadata
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (book.editionCount != null) {
                    MetaRow(
                        label = stringResource(R.string.editions),
                        value = book.editionCount.toString()
                    )
                }
                if (book.language.isNotEmpty()) {
                    MetaRow(
                        label = stringResource(R.string.language),
                        value = book.language.joinToString(", ")
                    )
                }
                if (book.isbn.isNotEmpty()) {
                    MetaRow(
                        label = stringResource(R.string.isbn),
                        value = book.isbn.joinToString(", ")
                    )
                }
                MetaRow(
                    label = stringResource(R.string.ebook_available),
                    value = if (book.hasFullText) {
                        stringResource(R.string.yes)
                    } else {
                        stringResource(R.string.no)
                    }
                )
            }
        }
    }
}

@Composable
private fun MetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(start = 16.dp)
        )
    }
}
