package com.anjor.bookscout.data.repository

import com.anjor.bookscout.data.remote.OpenLibraryApi
import com.anjor.bookscout.data.remote.dto.DocDto
import com.anjor.bookscout.data.remote.dto.SearchResponseDto
import com.anjor.bookscout.data.remote.dto.WorkDto
import com.anjor.bookscout.domain.model.SearchField
import com.anjor.bookscout.domain.model.SortOrder
import com.anjor.bookscout.util.ApiResult
import com.anjor.bookscout.util.ErrorType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

private class FakeApi(
    var searchResponse: SearchResponseDto? = null,
    var work: WorkDto? = null,
    var error: Throwable? = null
) : OpenLibraryApi {
    override suspend fun search(
        query: String?,
        title: String?,
        author: String?,
        page: Int,
        limit: Int,
        sort: String?,
        fields: String
    ): SearchResponseDto {
        error?.let { throw it }
        return searchResponse ?: SearchResponseDto()
    }

    override suspend fun getWork(workId: String): WorkDto {
        error?.let { throw it }
        return work ?: WorkDto()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class BookRepositoryTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Test
    fun `search returns success with hasMore computed from total`() = runTest {
        val api = FakeApi(
            searchResponse = SearchResponseDto(
                numFound = 25,
                start = 0,
                docs = List(20) { DocDto(key = "/works/OL$it", title = "B$it", coverI = 1) }
            )
        )
        val repo = BookRepository(api, dispatcher)

        val result = repo.searchBooks("kotlin", SearchField.ALL, 1, SortOrder.RELEVANCE, false)

        assertTrue(result is ApiResult.Success)
        val page = (result as ApiResult.Success).data
        assertEquals(20, page.books.size)
        assertEquals(25, page.total)
        assertTrue(page.hasMore)
    }

    @Test
    fun `onlyWithCovers filters out books without a cover`() = runTest {
        val docs = listOf(
            DocDto(key = "/works/OL1", title = "Has cover", coverI = 99),
            DocDto(key = "/works/OL2", title = "No cover", coverI = null)
        )
        val api = FakeApi(searchResponse = SearchResponseDto(numFound = 2, start = 0, docs = docs))
        val repo = BookRepository(api, dispatcher)

        val result = repo.searchBooks("x", SearchField.ALL, 1, SortOrder.RELEVANCE, true) as ApiResult.Success
        assertEquals(1, result.data.books.size)
        assertEquals("Has cover", result.data.books.first().title)
    }

    @Test
    fun `getBookDetails returns success`() = runTest {
        val api = FakeApi(work = WorkDto(key = "/works/OL9", title = "Detail"))
        val repo = BookRepository(api, dispatcher)

        val result = repo.getBookDetails("OL9", null)
        assertTrue(result is ApiResult.Success)
        assertEquals("Detail", (result as ApiResult.Success).data.title)
    }

    @Test
    fun `io_exception maps to NETWORK error`() = runTest {
        val api = FakeApi(error = IOException("offline"))
        val result = BookRepository(api, dispatcher)
            .searchBooks("x", SearchField.ALL, 1, SortOrder.RELEVANCE, false)
        assertTrue(result is ApiResult.Failure)
        assertEquals(ErrorType.NETWORK, (result as ApiResult.Failure).error.type)
    }

    @Test
    fun `socket_timeout maps to TIMEOUT error`() = runTest {
        val api = FakeApi(error = SocketTimeoutException("slow"))
        val result = BookRepository(api, dispatcher)
            .searchBooks("x", SearchField.ALL, 1, SortOrder.RELEVANCE, false)
        assertTrue(result is ApiResult.Failure)
        assertEquals(ErrorType.TIMEOUT, (result as ApiResult.Failure).error.type)
    }

    @Test
    fun `http_exception maps to HTTP error`() = runTest {
        val response = Response.Builder()
            .request(Request.Builder().url("https://openlibrary.org/search.json").build())
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Server Error")
            .body("".toResponseBody("application/json".toMediaType()))
            .build()
        val api = FakeApi(error = HttpException(response))
        val result = BookRepository(api, dispatcher)
            .searchBooks("x", SearchField.ALL, 1, SortOrder.RELEVANCE, false)
        assertTrue(result is ApiResult.Failure)
        assertEquals(ErrorType.HTTP, (result as ApiResult.Failure).error.type)
    }

    @Test
    fun `serialization_exception maps to SERIALIZATION error`() = runTest {
        val api = FakeApi(error = kotlinx.serialization.SerializationException("bad json"))
        val result = BookRepository(api, dispatcher)
            .searchBooks("x", SearchField.ALL, 1, SortOrder.RELEVANCE, false)
        assertTrue(result is ApiResult.Failure)
        assertEquals(ErrorType.SERIALIZATION, (result as ApiResult.Failure).error.type)
    }
}
