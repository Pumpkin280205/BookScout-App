package com.anjor.bookscout.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.data.repository.BookRepository
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.ui.details.DetailsViewModel
import com.anjor.bookscout.ui.search.SearchViewModel
import com.anjor.bookscout.ui.settings.SettingsViewModel

/**
 * Factory for ViewModels that need no navigation arguments (Search & Settings).
 * Reads dependencies from the shared [AppContainer].
 */
class AppViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SearchViewModel::class.java) ->
                SearchViewModel(container.bookRepository, container.settingsRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.settingsRepository) as T
            else -> throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}

/**
 * Factory for [DetailsViewModel], which requires navigation arguments
 * (work id + fallback data from the clicked search card).
 */
class DetailsViewModelFactory(
    private val repository: BookRepository,
    private val workId: String,
    private val fallback: BookSummary?
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailsViewModel::class.java)) {
            return DetailsViewModel(repository, workId, fallback) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
