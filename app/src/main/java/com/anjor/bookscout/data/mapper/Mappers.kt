package com.anjor.bookscout.data.mapper

import com.anjor.bookscout.data.remote.dto.DocDto
import com.anjor.bookscout.data.remote.dto.WorkDto
import com.anjor.bookscout.domain.model.BookDetails
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.util.Constants
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/** Small helper: blank string -> null. */
private fun String?.notBlankOrNull(): String? = if (this.isNullOrBlank()) null else this

/** Build a cover image URL, preferring the numeric cover id, then falling back to ISBN. */
fun buildCoverUrl(coverId: Int?, isbn: List<String>?): String? {
    if (coverId != null) {
        return "${Constants.COVER_BASE_URL}id/$coverId-L.jpg"
    }
    val firstIsbn = isbn?.firstOrNull()?.takeIf { it.isNotBlank() }
    return if (firstIsbn != null) "${Constants.COVER_BASE_URL}isbn/$firstIsbn-L.jpg" else null
}

/** Build the public Open Library web URL for a work key (e.g. "/works/OL27448W"). */
fun openLibraryWorkUrl(key: String?): String {
    if (key.isNullOrBlank()) return Constants.OPEN_LIBRARY_WEB
    val path = if (key.startsWith("/")) key else "/$key"
    return Constants.OPEN_LIBRARY_WEB + path
}

/** Resolve the polymorphic `description` field into a plain string (or null). */
fun JsonElement?.toPlainText(): String? {
    if (this == null) return null
    return try {
        when {
            isJsonPrimitive -> jsonPrimitive.content.notBlankOrNull()
            isJsonObject -> jsonObject["value"]?.jsonPrimitive?.content?.notBlankOrNull()
            else -> null
        }
    } catch (_: Exception) {
        null
    }
}

/** Map a search hit DTO to the domain [BookSummary]. */
fun DocDto.toBookSummary(): BookSummary {
    val key = key.orEmpty()
    val isbnList = isbn?.filter { it.isNotBlank() }.orEmpty()
    return BookSummary(
        key = key,
        title = title?.takeIf { it.isNotBlank() } ?: "Unknown title",
        authors = authorName?.filter { it.isNotBlank() }.orEmpty(),
        firstPublishYear = firstPublishYear,
        coverId = coverI,
        coverUrl = buildCoverUrl(coverI, isbnList),
        isbn = isbnList,
        subject = subject?.filter { it.isNotBlank() }.orEmpty(),
        language = language?.filter { it.isNotBlank() }.orEmpty(),
        editionCount = editionCount,
        hasFullText = hasFullText ?: false,
        openLibraryUrl = openLibraryWorkUrl(key)
    )
}

/**
 * Map a work DTO to [BookDetails], filling gaps from the search [fallback] when the work
 * endpoint is incomplete (it does not include ISBN, language or edition count).
 */
fun WorkDto.toBookDetails(fallback: BookSummary?): BookDetails {
    val resolvedKey = key ?: fallback?.key
    val coverId = covers?.firstOrNull() ?: fallback?.coverId
    val workAuthors = authors
        ?.mapNotNull { it.name.notBlankOrNull() }
        ?.takeIf { it.isNotEmpty() }
        ?: fallback?.authors
        ?: emptyList()

    return BookDetails(
        key = resolvedKey ?: "",
        title = title?.takeIf { it.isNotBlank() } ?: fallback?.title ?: "Unknown title",
        authors = workAuthors,
        description = description.toPlainText(),
        subjects = subjects?.filter { it.isNotBlank() } ?: fallback?.subject ?: emptyList(),
        firstPublishYear = fallback?.firstPublishYear,
        firstPublishDate = firstPublishDate.notBlankOrNull(),
        coverId = coverId,
        coverUrl = buildCoverUrl(coverId, fallback?.isbn),
        isbn = fallback?.isbn ?: emptyList(),
        language = fallback?.language ?: emptyList(),
        editionCount = fallback?.editionCount,
        hasFullText = fallback?.hasFullText ?: false,
        openLibraryUrl = openLibraryWorkUrl(resolvedKey)
    )
}
