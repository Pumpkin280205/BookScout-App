package com.anjor.bookscout

import android.app.Application
import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.data.repository.BookRepository
import com.anjor.bookscout.di.AppContainer
import com.anjor.bookscout.di.NetworkModule

/**
 * Application class that wires up the dependency graph once and exposes it via [appContainer].
 */
class BookScoutApplication : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        val api = NetworkModule.provideOpenLibraryApi()
        val bookRepository = BookRepository(api)
        val settingsRepository = UserSettingsRepository(this)

        appContainer = AppContainer(
            api = api,
            bookRepository = bookRepository,
            settingsRepository = settingsRepository
        )
    }
}
