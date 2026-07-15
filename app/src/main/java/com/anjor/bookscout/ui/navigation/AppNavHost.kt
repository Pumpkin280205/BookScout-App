package com.anjor.bookscout.ui.navigation

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.anjor.bookscout.data.mapper.buildCoverUrl
import com.anjor.bookscout.di.AppViewModelFactory
import com.anjor.bookscout.di.DetailsViewModelFactory
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.ui.LocalAppContainer
import com.anjor.bookscout.ui.details.DetailsScreen
import com.anjor.bookscout.ui.details.DetailsViewModel
import com.anjor.bookscout.ui.search.SearchScreen
import com.anjor.bookscout.ui.search.SearchViewModel
import com.anjor.bookscout.ui.settings.SettingsScreen
import com.anjor.bookscout.ui.settings.SettingsViewModel

const val ROUTE_SEARCH = "search"
const val ROUTE_SETTINGS = "settings"
const val ROUTE_DETAILS = "details/{workId}?title={title}&coverId={coverId}"

/** Build the navigation route for a book's details screen, URL-encoding the title. */
fun detailsRoute(workId: String, title: String, coverId: Int?): String {
    val encTitle = Uri.encode(title)
    val cover = if (coverId != null) "?coverId=$coverId" else ""
    return "details/$workId?title=$encTitle$cover"
}

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController()
) {
    val container = LocalAppContainer.current

    NavHost(navController = navController, startDestination = ROUTE_SEARCH) {

        composable(ROUTE_SEARCH) {
            val factory = remember(container) { AppViewModelFactory(container) }
            val vm: SearchViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            val state by vm.uiState.collectAsStateWithLifecycle()

            SearchScreen(
                uiState = state,
                onQueryChange = vm::onQueryChange,
                onSearchFieldChange = vm::onSearchFieldChange,
                onSearch = vm::search,
                onBookClick = { book ->
                    val workId = book.key.substringAfterLast("/")
                    navController.navigate(detailsRoute(workId, book.title, book.coverId))
                },
                onLoadMore = vm::loadMore,
                onRetry = vm::retry,
                onRecentSearchClick = vm::search,
                onClearRecent = vm::clearRecentSearches,
                onSettingsClick = { navController.navigate(ROUTE_SETTINGS) }
            )
        }

        composable(ROUTE_SETTINGS) {
            val factory = remember(container) { AppViewModelFactory(container) }
            val vm: SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
            val settings by vm.settings.collectAsStateWithLifecycle()

            SettingsScreen(
                settings = settings,
                onThemeChange = vm::setThemeMode,
                onSortChange = vm::setSortOrder,
                onOnlyCoversChange = vm::setOnlyWithCovers,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = ROUTE_DETAILS,
            arguments = listOf(
                navArgument("workId") { type = NavType.StringType },
                navArgument("title") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = ""
                },
                navArgument("coverId") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val workId = backStackEntry.arguments?.getString("workId").orEmpty()
            val title = backStackEntry.arguments?.getString("title").orEmpty()
            val coverId = backStackEntry.arguments?.getInt("coverId")?.takeIf { it > 0 }

            // Reconstruct a minimal fallback from the search card so the detail screen
            // looks complete while the work endpoint loads (or if it is incomplete).
            val fallback = BookSummary(
                key = "/works/$workId",
                title = title.ifBlank { "Unknown title" },
                authors = emptyList(),
                firstPublishYear = null,
                coverId = coverId,
                coverUrl = buildCoverUrl(coverId, null),
                isbn = emptyList(),
                subject = emptyList(),
                language = emptyList(),
                editionCount = null,
                hasFullText = false,
                openLibraryUrl = "https://openlibrary.org/works/$workId"
            )

            val factory = remember(workId) {
                DetailsViewModelFactory(container.bookRepository, workId, fallback)
            }
            val vm: DetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                viewModelStoreOwner = backStackEntry,
                factory = factory
            )
            val state by vm.uiState.collectAsStateWithLifecycle()

            DetailsScreen(
                uiState = state,
                onRetry = vm::retry,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
