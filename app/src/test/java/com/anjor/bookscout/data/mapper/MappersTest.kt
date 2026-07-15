package com.anjor.bookscout.data.mapper

import com.anjor.bookscout.data.remote.dto.AuthorRefDto
import com.anjor.bookscout.data.remote.dto.DocDto
import com.anjor.bookscout.data.remote.dto.WorkDto
import com.anjor.bookscout.domain.model.BookSummary
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MappersTest {

    @Test
    fun `toBookSummary falls back gracefully when fields are missing`() {
        val dto = DocDto(key = "/works/OL1W") // everything else null
        val book = dto.toBookSummary()

        assertEquals("Unknown title", book.title)
        assertEquals(emptyList<String>(), book.authors)
        assertNull(book.coverId)
        assertNull(book.coverUrl)
        assertEquals("https://openlibrary.org/works/OL1W", book.openLibraryUrl)
    }

    @Test
    fun `toBookSummary builds cover url from cover id`() {
        val dto = DocDto(
            key = "/works/OL2W",
            title = "Kotlin in Action",
            coverI = 12345,
            isbn = listOf("9781617293290")
        )
        val book = dto.toBookSummary()
        assertEquals("https://covers.openlibrary.org/b/id/12345-L.jpg", book.coverUrl)
    }

    @Test
    fun `buildCoverUrl falls back to isbn when no cover id`() {
        val url = buildCoverUrl(null, listOf("9781617293290"))
        assertEquals("https://covers.openlibrary.org/b/isbn/9781617293290-L.jpg", url)
    }

    @Test
    fun `buildCoverUrl returns null when nothing available`() {
        assertNull(buildCoverUrl(null, emptyList()))
    }

    @Test
    fun `description parsed from plain string`() {
        val dto = WorkDto(
            key = "/works/OL3W",
            title = "Book",
            description = JsonPrimitive("A plain description.")
        )
        val details = dto.toBookDetails(null)
        assertEquals("A plain description.", details.description)
    }

    @Test
    fun `description parsed from object form`() {
        val dto = WorkDto(
            key = "/works/OL4W",
            title = "Book",
            description = JsonObject(mapOf("type" to JsonPrimitive("text"), "value" to JsonPrimitive("Object form.")))
        )
        val details = dto.toBookDetails(null)
        assertEquals("Object form.", details.description)
    }

    @Test
    fun `toBookDetails merges fallback when work is incomplete`() {
        val fallback = BookSummary(
            key = "/works/OL5W",
            title = "Fallback Title",
            authors = listOf("Jane Doe"),
            firstPublishYear = 1999,
            coverId = 999,
            coverUrl = "https://covers.openlibrary.org/b/id/999-L.jpg",
            isbn = listOf("123"),
            subject = listOf("Fiction"),
            language = listOf("eng"),
            editionCount = 3,
            hasFullText = true,
            openLibraryUrl = "https://openlibrary.org/works/OL5W"
        )
        val work = WorkDto(
            key = "/works/OL5W",
            title = null, // missing -> use fallback
            authors = null,
            covers = null,
            subjects = listOf("Science")
        )
        val details = work.toBookDetails(fallback)
        assertEquals("Fallback Title", details.title)
        assertEquals(listOf("Jane Doe"), details.authors)
        assertEquals(999, details.coverId)
        assertEquals(listOf("Science"), details.subjects) // work wins when present
        assertEquals(listOf("123"), details.isbn)         // only in fallback
        assertEquals(true, details.hasFullText)
    }

    @Test
    fun `toBookDetails prefers work authors over fallback`() {
        val work = WorkDto(
            key = "/works/OL6W",
            title = "T",
            authors = listOf(AuthorRefDto(name = "Real Author"))
        )
        val details = work.toBookDetails(null)
        assertEquals(listOf("Real Author"), details.authors)
    }
}
