package com.anjor.bookscout.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.data.repository.BookRepository
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.domain.model.SearchField
import com.anjor.bookscout.util.ApiResult
import com.anjor.bookscout.util.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Lifecycle of the results area. */
enum class SearchStatus {
    IDLE,      // Nothing searched yet (show search prompt / recent searches)
    LOADING,   // First page loading
    SUCCESS,   // Results shown
    EMPTY,     // Search returned zero books
    ERROR      // Request failed
}

/** Immutable snapshot of the search screen, rendered by [SearchScreen]. */
data class SearchUiState(
    val query: String = "",
    val searchField: SearchField = SearchField.ALL,
    val status: SearchStatus = SearchStatus.IDLE,
    val books: List<BookSummary> = emptyList(),
    val totalResults: Int = 0,
    val currentPage: Int = 1,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val error: AppError? = null,
    val recentSearches: List<String> = emptyList()
)

/**
 * Drives the search screen: input, validation, first-page search, pagination ("load more"),
 * retry and reacting to settings changes (sort / covers-only filter).
 */
class SearchViewModel(
    private val repository: BookRepository,
    private val settingsRepository: UserSettingsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Settings currently in effect, so we can re-run a search when they change.
    private val settings = MutableStateFlow(
        com.anjor.bookscout.data.local.UserSettings()
    )
    private var lastSort = settings.value.sortOrder
    private var lastOnlyCovers = settings.value.onlyWithCovers

    init {
        viewModelScope.launch {
            settingsRepository.settingsFlow.collect { s ->
                val query = _uiState.value.query
                val status = _uiState.value.status
                _uiState.update { it.copy(recentSearches = s.recentSearches) }

                // Re-run an active search only when sort / covers filter actually changed
                // (not when only the recent-searches list updated) to avoid loops.
                val settingsChanged = s.sortOrder != lastSort || s.onlyWithCovers != lastOnlyCovers
                if (settingsChanged && query.isNotBlank() && status in listOf(
                        SearchStatus.SUCCESS, SearchStatus.EMPTY
                    )
                ) {
                    search(query)
                }
                lastSort = s.sortOrder
                lastOnlyCovers = s.onlyWithCovers
                settings.value = s
            }
        }
    }

    fun onQueryChange(value: String) = _uiState.update { it.copy(query = value) }

    fun onSearchFieldChange(field: SearchField) =
        _uiState.update { it.copy(searchField = field) }

    /** Validate and run a new search (page 1). Ignores blank queries and repeat presses. */
    fun search(overrideQuery: String? = null) {
        val query = overrideQuery?.trim() ?: _uiState.value.query.trim()
        if (query.isBlank()) {
            // Input validation: do not crash on empty input, just stay idle.
            _uiState.update { it.copy(status = SearchStatus.IDLE, error = null) }
            return
        }
        if (_uiState.value.status == SearchStatus.LOADING) return // guard repeated taps

        viewModelScope.launch { settingsRepository.addRecentSearch(query) }
        performSearch(query, page = 1, isLoadMore = false)
    }

    /** Append the next page of results. No-op while already loading or with nothing left. */
    fun loadMore() {
        val state = _uiState.value
        if (state.isLoadingMore || !state.canLoadMore || state.status != SearchStatus.SUCCESS) return
        performSearch(state.query, page = state.currentPage + 1, isLoadMore = true)
    }

    fun retry() = search(_uiState.value.query)

    fun clearRecentSearches() = viewModelScope.launch {
        settingsRepository.clearRecentSearches()
    }

    fun removeRecentSearch(query: String) = viewModelScope.launch {
        settingsRepository.removeRecentSearch(query)
    }

    private fun performSearch(query: String, page: Int, isLoadMore: Boolean) {
        viewModelScope.launch {
            _uiState.update {
                if (isLoadMore) {
                    it.copy(isLoadingMore = true, error = null)
                } else {
                    it.copy(status = SearchStatus.LOADING, query = query, error = null)
                }
            }

            val sort = settings.value.sortOrder
            val onlyCovers = settings.value.onlyWithCovers
            val field = _uiState.value.searchField

            when (val result = repository.searchBooks(query, field, page, sort, onlyCovers)) {
                is ApiResult.Success -> {
                    val pageData = result.data
                    _uiState.update { state ->
                        val merged =
                            if (isLoadMore) state.books + pageData.books else pageData.books
                        state.copy(
                            status = if (merged.isEmpty()) SearchStatus.EMPTY else SearchStatus.SUCCESS,
                            books = merged,
                            totalResults = pageData.total,
                            currentPage = pageData.page,
                            canLoadMore = pageData.hasMore,
                            isLoadingMore = false,
                            error = null
                        )
                    }
                }

                is ApiResult.Failure -> {
                    _uiState.update { state ->
                        if (isLoadMore) {
                            state.copy(isLoadingMore = false)
                        } else {
                            state.copy(
                                status = SearchStatus.ERROR,
                                error = result.error,
                                isLoadingMore = false
                            )
                        }
                    }
                }
            }
        }
    }
}
