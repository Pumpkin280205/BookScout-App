package com.anjor.bookscout.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.anjor.bookscout.R
import com.anjor.bookscout.util.AppError
import com.anjor.bookscout.util.ErrorType

/** Friendly, centered message shown when a search returns zero results. */
@Composable
fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.SearchOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = stringResource(R.string.empty_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        Text(
            text = stringResource(R.string.empty_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, start = 32.dp, end = 32.dp)
        )
    }
}

/** Friendly, centered error with a Retry button. The message depends on the error type. */
@Composable
fun ErrorState(
    error: AppError?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val message = when (error?.type) {
        ErrorType.NETWORK -> stringResource(R.string.error_network)
        ErrorType.TIMEOUT -> stringResource(R.string.error_timeout)
        ErrorType.HTTP -> stringResource(R.string.error_http)
        ErrorType.SERIALIZATION -> stringResource(R.string.error_serialization)
        else -> stringResource(R.string.error_unknown)
    }
    val detail = error?.message?.takeIf { it.isNotBlank() }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.CloudOff,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(72.dp)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, start = 32.dp, end = 32.dp)
        )
        if (detail != null) {
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp, start = 40.dp, end = 40.dp)
            )
        }
        Button(onClick = onRetry, modifier = Modifier.padding(top = 20.dp)) {
            Icon(Icons.Filled.WarningAmber, contentDescription = null)
            Text(
                text = stringResource(R.string.action_retry),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
