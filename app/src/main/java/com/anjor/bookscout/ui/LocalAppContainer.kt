package com.anjor.bookscout.ui

import androidx.compose.runtime.compositionLocalOf
import com.anjor.bookscout.di.AppContainer

/**
 * Composition-local holding the app-wide dependency container so screens can build their
 * ViewModels without threading the container through every composable.
 */
val LocalAppContainer = compositionLocalOf<AppContainer> {
    error("AppContainer not provided")
}
