package com.anjor.bookscout.data.remote

import com.anjor.bookscout.data.remote.dto.SearchResponseDto
import com.anjor.bookscout.data.remote.dto.WorkDto
import com.anjor.bookscout.util.Constants
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit service describing the two Open Library endpoints BookScout uses.
 *
 * Base URL is [Constants.BASE_URL] ("https://openlibrary.org/"), so the paths below are
 * relative (`search.json`, `works/{workId}.json`).
 */
interface OpenLibraryApi {

    /**
     * Search the Open Library catalogue.
     *
     * Depending on [field] we target a specific query parameter:
     * - [SearchField.ALL]     -> `q`
     * - [SearchField.TITLE]   -> `title`
     * - [SearchField.AUTHOR]  -> `author`
     *
     * @param page  1-based page number used for pagination.
     * @param sort  maps to the API `sort` param (null = relevance, the API default).
     */
    @GET("search.json")
    suspend fun search(
        @Query("q") query: String? = null,
        @Query("title") title: String? = null,
        @Query("author") author: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = Constants.DEFAULT_PAGE_SIZE,
        @Query("sort") sort: String? = null,
        @Query("fields") fields: String = Constants.SEARCH_FIELDS
    ): SearchResponseDto

    /** Fetch a single work (book) by its id, e.g. "OL27448W". */
    @GET("works/{workId}.json")
    suspend fun getWork(@Path("workId") workId: String): WorkDto
}
