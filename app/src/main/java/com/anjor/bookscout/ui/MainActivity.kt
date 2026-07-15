package com.anjor.bookscout.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.anjor.bookscout.BookScoutApplication
import com.anjor.bookscout.data.local.UserSettings
import com.anjor.bookscout.domain.model.ThemeMode
import com.anjor.bookscout.ui.navigation.AppNavHost
import com.anjor.bookscout.ui.theme.BookScoutTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the Android 12+ splash screen before anything else.
        installSplashScreen()
        // Switch from the splash theme to the normal app theme.
        setTheme(R.style.Theme_BookScout)

        super.onCreate()

        val container = (application as BookScoutApplication).appContainer

        setContent {
            val settings by container.settingsRepository.settingsFlow
                .collectAsState(initial = UserSettings())

            val darkTheme = when (settings.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }

            CompositionLocalProvider(LocalAppContainer provides container) {
                BookScoutTheme(darkTheme = darkTheme) {
                    AppNavHost()
                }
            }
        }
    }
}
