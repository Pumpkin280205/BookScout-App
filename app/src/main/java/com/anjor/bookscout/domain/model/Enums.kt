package com.anjor.bookscout.domain.model

/** Which field the search box should target on the Open Library API. */
enum class SearchField(val label: String) {
    ALL("All"),
    TITLE("Title"),
    AUTHOR("Author")
}

/**
 * Sort order for search results. The API uses the `sort` query parameter; `RELEVANCE`
 * maps to the API default (no explicit value sent).
 */
enum class SortOrder(val label: String, val apiValue: String?) {
    RELEVANCE("Relevance", null),
    NEWEST("Newest first", "new"),
    OLDEST("Oldest first", "old"),
    TITLE("Title A–Z", "title")
}

/** Theme preference stored in DataStore. */
enum class ThemeMode(val label: String) {
    SYSTEM("System"),
    LIGHT("Light"),
    DARK("Dark")
}
