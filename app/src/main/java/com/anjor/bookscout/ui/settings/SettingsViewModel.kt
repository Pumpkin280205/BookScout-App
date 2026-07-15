package com.anjor.bookscout.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anjor.bookscout.data.local.UserSettings
import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.domain.model.SortOrder
import com.anjor.bookscout.domain.model.ThemeMode
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes [UserSettings] as state and offers setters that persist to DataStore.
 */
class SettingsViewModel(private val repository: UserSettingsRepository) : ViewModel() {

    val settings: StateFlow<UserSettings> = repository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings()
    )

    fun setThemeMode(mode: ThemeMode) = viewModelScope.launch {
        repository.setThemeMode(mode)
    }

    fun setSortOrder(order: SortOrder) = viewModelScope.launch {
        repository.setSortOrder(order)
    }

    fun setOnlyWithCovers(value: Boolean) = viewModelScope.launch {
        repository.setOnlyWithCovers(value)
    }
}
