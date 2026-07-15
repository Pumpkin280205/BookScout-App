package com.anjor.bookscout.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anjor.bookscout.data.repository.BookRepository
import com.anjor.bookscout.domain.model.BookDetails
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.util.ApiResult
import com.anjor.bookscout.util.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class DetailsStatus { LOADING, SUCCESS, ERROR }

data class DetailsUiState(
    val status: DetailsStatus = DetailsStatus.LOADING,
    val book: BookDetails? = null,
    val error: AppError? = null
)

/**
 * Loads a single book's details. [fallback] supplies cover/title/authors from the search
 * card so the screen looks complete even when the work endpoint is missing fields.
 */
class DetailsViewModel(
    private val repository: BookRepository,
    private val workId: String,
    private val fallback: BookSummary?
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailsUiState())
    val uiState: StateFlow<DetailsUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(status = DetailsStatus.LOADING, error = null) }
            when (val result = repository.getBookDetails(workId, fallback)) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(status = DetailsStatus.SUCCESS, book = result.data, error = null)
                }
                is ApiResult.Failure -> _uiState.update {
                    it.copy(status = DetailsStatus.ERROR, error = result.error)
                }
            }
        }
    }

    fun retry() = load()
}
