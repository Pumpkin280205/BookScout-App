package com.anjor.bookscout.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top level response of the Open Library search API:
 * `https://openlibrary.org/search.json`
 *
 * All fields are optional with safe defaults so that a partial / malformed
 * response never throws during deserialization.
 */
@Serializable
data class SearchResponseDto(
    @SerialName("numFound") val numFound: Int = 0,
    @SerialName("start") val start: Int = 0,
    @SerialName("numFoundExact") val numFoundExact: Boolean? = null,
    @SerialName("docs") val docs: List<DocDto> = emptyList(),
    @SerialName("q") val q: String? = null
)

/**
 * A single search hit. The Open Library schema is inconsistent: not every book has a
 * cover, author, year, subject, ISBN or language, so every field is nullable with a
 * default to keep [kotlinx.serialization] happy.
 */
@Serializable
data class DocDto(
    @SerialName("key") val key: String? = null,
    @SerialName("title") val title: String? = null,
    @SerialName("author_name") val authorName: List<String>? = null,
    @SerialName("first_publish_year") val firstPublishYear: Int? = null,
    @SerialName("cover_i") val coverI: Int? = null,
    @SerialName("cover_edition_key") val coverEditionKey: String? = null,
    @SerialName("isbn") val isbn: List<String>? = null,
    @SerialName("subject") val subject: List<String>? = null,
    @SerialName("language") val language: List<String>? = null,
    @SerialName("publish_year") val publishYear: List<Int>? = null,
    @SerialName("edition_count") val editionCount: Int? = null,
    @SerialName("has_fulltext") val hasFullText: Boolean? = null,
    @SerialName("ia") val ia: List<String>? = null
)
