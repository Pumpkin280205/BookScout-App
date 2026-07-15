package com.anjor.bookscout.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.anjor.bookscout.domain.model.SortOrder
import com.anjor.bookscout.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "bookscout_settings")

/**
 * User preferences persisted with Jetpack DataStore (Preferences). Holds display
 * preferences (theme, sort order, "covers only" filter) plus the list of recent searches.
 */
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val sortOrder: SortOrder = SortOrder.RELEVANCE,
    val onlyWithCovers: Boolean = false,
    val recentSearches: List<String> = emptyList()
)

open class UserSettingsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val SORT = stringPreferencesKey("sort_order")
        val ONLY_COVERS = androidx.datastore.preferences.core.booleanPreferencesKey("only_with_covers")
        val RECENT = stringSetPreferencesKey("recent_searches")
    }

    open val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        val theme = prefs[Keys.THEME]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
            ?: ThemeMode.SYSTEM
        val sort = prefs[Keys.SORT]?.let { runCatching { SortOrder.valueOf(it) }.getOrNull() }
            ?: SortOrder.RELEVANCE
        val onlyCovers = prefs[Keys.ONLY_COVERS] ?: false
        // Keep most-recent first and drop the internal ordering markers.
        val recent = prefs[Keys.RECENT].orEmpty()
            .mapNotNull { it.removePrefix(RECENT_PREFIX).takeIf { v -> v.isNotBlank() } }
            .sortedByDescending { it.substringBefore('|').toLongOrNull() ?: 0L }
            .map { it.substringAfter('|') }
            .distinct()
            .take(MAX_RECENT)
        UserSettings(theme, sort, onlyCovers, recent)
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME] = mode.name }
    }

    suspend fun setSortOrder(order: SortOrder) {
        context.dataStore.edit { it[Keys.SORT] = order.name }
    }

    suspend fun setOnlyWithCovers(value: Boolean) {
        context.dataStore.edit { it[Keys.ONLY_COVERS] = value }
    }

    /** Record a search term, de-duplicating and keeping the [MAX_RECENT] most recent. */
    open suspend fun addRecentSearch(query: String) {
        val q = query.trim()
        if (q.isBlank()) return
        val stamp = System.currentTimeMillis()
        context.dataStore.edit { prefs ->
            val existing = prefs[Keys.RECENT].orEmpty()
                // drop any previous entry for the same query
                .filter { it.substringAfter('|') != q }
                .toMutableList()
            existing.add(0, "$stamp$RECENT_PREFIX$q")
            prefs[Keys.RECENT] = existing.distinct().takeLast(MAX_RECENT)
        }
    }

    suspend fun removeRecentSearch(query: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.RECENT] = prefs[Keys.RECENT].orEmpty()
                .filter { it.substringAfter('|') != query }
                .toSet()
        }
    }

    suspend fun clearRecentSearches() {
        context.dataStore.edit { it.remove(Keys.RECENT) }
    }

    private companion object {
        const val RECENT_PREFIX = "|"
        const val MAX_RECENT = 8
    }
}
