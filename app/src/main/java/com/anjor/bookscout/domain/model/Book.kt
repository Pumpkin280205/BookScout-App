package com.anjor.bookscout.domain.model

/**
 * A compact book representation used in search results / cover cards.
 * Every field that may be missing from the API is nullable so the UI can degrade
 * gracefully instead of crashing.
 */
data class BookSummary(
    /** Full work path as returned by the API, e.g. "/works/OL27448W". */
    val key: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val coverId: Int?,
    val coverUrl: String?,
    val isbn: List<String>,
    val subject: List<String>,
    val language: List<String>,
    val editionCount: Int?,
    val hasFullText: Boolean,
    val openLibraryUrl: String
)

/** A single page of search results. */
data class SearchPage(
    val books: List<BookSummary>,
    val total: Int,
    val page: Int,
    val hasMore: Boolean
)

/**
 * Rich book representation used on the details screen. Combines data from the work
 * endpoint with fallbacks taken from the search result card when the work is incomplete.
 */
data class BookDetails(
    val key: String,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val subjects: List<String>,
    val firstPublishYear: Int?,
    val firstPublishDate: String?,
    val coverId: Int?,
    val coverUrl: String?,
    val isbn: List<String>,
    val language: List<String>,
    val editionCount: Int?,
    val hasFullText: Boolean,
    val openLibraryUrl: String
)
