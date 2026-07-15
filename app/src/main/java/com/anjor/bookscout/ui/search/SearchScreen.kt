package com.anjor.bookscout.ui.search

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.menuAnchor
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.anjor.bookscout.R
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.domain.model.SearchField
import com.anjor.bookscout.ui.components.BookCoverCard
import com.anjor.bookscout.ui.components.BooksShimmerGrid
import com.anjor.bookscout.ui.components.EmptyState
import com.anjor.bookscout.ui.components.ErrorState
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onQueryChange: (String) -> Unit,
    onSearchFieldChange: (SearchField) -> Unit,
    onSearch: () -> Unit,
    onBookClick: (BookSummary) -> Unit,
    onLoadMore: () -> Unit,
    onRetry: () -> Unit,
    onRecentSearchClick: (String) -> Unit,
    onClearRecent: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = stringResource(R.string.action_settings))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = uiState.query,
                searchField = uiState.searchField,
                onQueryChange = onQueryChange,
                onSearchFieldChange = onSearchFieldChange,
                onSearch = onSearch
            )

            when (uiState.status) {
                SearchStatus.IDLE -> {
                    IdleContent(
                        recentSearches = uiState.recentSearches,
                        onRecentSearchClick = onRecentSearchClick,
                        onClearRecent = onClearRecent
                    )
                }

                SearchStatus.LOADING -> {
                    BooksShimmerGrid()
                }

                SearchStatus.EMPTY -> {
                    EmptyState()
                }

                SearchStatus.ERROR -> {
                    ErrorState(error = uiState.error, onRetry = onRetry)
                }

                SearchStatus.SUCCESS -> {
                    ResultsGrid(
                        books = uiState.books,
                        canLoadMore = uiState.canLoadMore,
                        isLoadingMore = uiState.isLoadingMore,
                        onBookClick = onBookClick,
                        onLoadMore = onLoadMore
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchBar(
    query: String,
    searchField: SearchField,
    onQueryChange: (String) -> Unit,
    onSearchFieldChange: (SearchField) -> Unit,
    onSearch: () -> Unit
) {
    var fieldMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = fieldMenuExpanded,
                onExpandedChange = { fieldMenuExpanded = it },
                modifier = Modifier.weight(0.42f)
            ) {
                OutlinedTextField(
                    value = searchField.label,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.search_in)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fieldMenuExpanded) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    singleLine = true
                )
                ExposedDropdownMenu(
                    expanded = fieldMenuExpanded,
                    onDismissRequest = { fieldMenuExpanded = false }
                ) {
                    SearchField.values().forEach { field ->
                        DropdownMenuItem(
                            text = { Text(field.label) },
                            onClick = {
                                onSearchFieldChange(field)
                                fieldMenuExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text(stringResource(R.string.search_hint)) },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = if (query.isNotBlank()) {
                    {
                        IconButton(onClick = { onQueryChange("") }) {
                            Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.action_clear))
                        }
                    }
                } else null,
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { onSearch() }
                ),
                modifier = Modifier.weight(1f)
            )
        }

        Button(
            onClick = onSearch,
            enabled = query.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.Search, contentDescription = null)
            Text(
                text = stringResource(R.string.action_search),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun IdleContent(
    recentSearches: List<String>,
    onRecentSearchClick: (String) -> Unit,
    onClearRecent: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.search_prompt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(visible = recentSearches.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.recent_searches),
                            style = MaterialTheme.typography.titleSmall
                        )
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                            TextButton(onClick = onClearRecent) {
                                Text(stringResource(R.string.action_clear_all))
                            }
                        }
                    }
                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        recentSearches.forEach { term ->
                            AssistChip(
                                onClick = { onRecentSearchClick(term) },
                                label = { Text(term) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Filled.History,
                                        contentDescription = null,
                                        modifier = Modifier.sizeIn(maxWidth = 18.dp, maxHeight = 18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultsGrid(
    books: List<BookSummary>,
    canLoadMore: Boolean,
    isLoadingMore: Boolean,
    onBookClick: (BookSummary) -> Unit,
    onLoadMore: () -> Unit
) {
    val gridState = rememberLazyGridState()

    // Auto-trigger "load more" when the user scrolls near the end.
    LaunchedEffect(gridState, books.size, canLoadMore) {
        snapshotFlow { gridState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged()
            .collect { lastIndex ->
                if (canLoadMore && lastIndex >= books.size - 2) {
                    onLoadMore()
                }
            }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 150.dp),
        state = gridState,
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(books.size, key = { books[it].key + it }) { index ->
            BookCoverCard(book = books[index], onClick = { onBookClick(books[index]) })
        }

        if (canLoadMore) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(strokeWidth = 3.dp)
                    } else {
                        Button(onClick = onLoadMore) {
                            Text(stringResource(R.string.action_load_more))
                        }
                    }
                }
            }
        }
    }
}
