package com.anjor.bookscout.data.repository

import com.anjor.bookscout.data.mapper.toBookDetails
import com.anjor.bookscout.data.mapper.toBookSummary
import com.anjor.bookscout.data.remote.OpenLibraryApi
import com.anjor.bookscout.domain.model.BookSummary
import com.anjor.bookscout.domain.model.SearchField
import com.anjor.bookscout.domain.model.SearchPage
import com.anjor.bookscout.domain.model.SortOrder
import com.anjor.bookscout.util.ApiResult
import com.anjor.bookscout.util.AppError
import com.anjor.bookscout.util.Constants
import com.anjor.bookscout.util.ErrorType
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerializationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

/**
 * Single source of truth for book data. Wraps the [OpenLibraryApi] and converts every
 * possible failure (no internet, timeout, HTTP error, bad JSON) into a typed
 * [ApiResult.Failure] so the UI can react without catching exceptions.
 *
 * @param dispatcher the dispatcher used for network calls (defaults to [Dispatchers.IO],
 *        overridden in tests).
 */
class BookRepository(
    private val api: OpenLibraryApi,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    /**
     * Search books for [query].
     *
     * @param field  which field to search (All / Title / Author).
     * @param page   1-based page number for pagination.
     * @param sort   sort order; null means relevance (API default).
     * @param onlyWithCovers when true, books without a cover are filtered out client side.
     */
    suspend fun searchBooks(
        query: String,
        field: SearchField,
        page: Int,
        sort: SortOrder,
        onlyWithCovers: Boolean
    ): ApiResult<SearchPage> = safeCall {
        val response = when (field) {
            SearchField.ALL -> api.search(query = query, page = page, sort = sort.apiValue)
            SearchField.TITLE -> api.search(title = query, page = page, sort = sort.apiValue)
            SearchField.AUTHOR -> api.search(author = query, page = page, sort = sort.apiValue)
        }

        val total = response.numFound
        val rawDocs = response.docs
        val books: List<BookSummary> = rawDocs
            .map { it.toBookSummary() }
            .let { if (onlyWithCovers) it.filter { book -> book.coverId != null } else it }

        // Whether another page exists, computed from the raw response (before filtering).
        val hasMore = (response.start + rawDocs.size) < total

        SearchPage(
            books = books,
            total = total,
            page = page,
            hasMore = hasMore
        )
    }

    /** Fetch the full details of a work, merging in [fallback] data when the work is incomplete. */
    suspend fun getBookDetails(
        workId: String,
        fallback: BookSummary?
    ): ApiResult<BookDetails> = safeCall {
        val workIdClean = workId
            .removePrefix("/works/")
            .removePrefix("works/")
            .removePrefix("/")
        val dto = api.getWork(workIdClean)
        dto.toBookDetails(fallback)
    }

    /**
     * Runs [block] on the IO dispatcher and maps exceptions to typed failures.
     * Never throws past this boundary.
     */
    private suspend fun <T> safeCall(block: suspend () -> T): ApiResult<T> =
        withContext(dispatcher) {
            try {
                ApiResult.Success(block())
            } catch (e: HttpException) {
                ApiResult.Failure(
                    AppError(ErrorType.HTTP, "HTTP ${e.code()}: ${e.message()}")
                )
            } catch (e: SocketTimeoutException) {
                ApiResult.Failure(AppError(ErrorType.TIMEOUT, e.message))
            } catch (e: SerializationException) {
                ApiResult.Failure(AppError(ErrorType.SERIALIZATION, e.message))
            } catch (e: IOException) {
                ApiResult.Failure(AppError(ErrorType.NETWORK, e.message))
            } catch (e: Exception) {
                ApiResult.Failure(AppError(ErrorType.UNKNOWN, e.message))
            }
        }
}
