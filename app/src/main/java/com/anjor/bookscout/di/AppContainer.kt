package com.anjor.bookscout.di

import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.data.remote.OpenLibraryApi
import com.anjor.bookscout.data.repository.BookRepository

/**
 * Holds the singleton instances shared across the app. Built once in [BookScoutApplication].
 */
data class AppContainer(
    val api: OpenLibraryApi,
    val bookRepository: BookRepository,
    val settingsRepository: UserSettingsRepository
)
