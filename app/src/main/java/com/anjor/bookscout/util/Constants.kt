package com.anjor.bookscout.util

/**
 * Central place for network / API constants used across the data layer.
 */
object Constants {
    const val BASE_URL = "https://openlibrary.org/"
    const val COVER_BASE_URL = "https://covers.openlibrary.org/b/"
    const val DEFAULT_PAGE_SIZE = 20

    /**
     * The subset of fields we ask the search API to return. Requesting only what we need
     * keeps payloads small and responses fast.
     */
    const val SEARCH_FIELDS =
        "key,title,author_name,first_publish_year,cover_i,cover_edition_key,isbn," +
            "subject,language,publish_year,edition_count,has_fulltext,ia"

    const val OPEN_LIBRARY_WEB = "https://openlibrary.org"
}
