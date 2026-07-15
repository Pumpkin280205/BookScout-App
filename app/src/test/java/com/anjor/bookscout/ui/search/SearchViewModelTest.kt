package com.anjor.bookscout.ui.search

import com.anjor.bookscout.data.local.UserSettings
import com.anjor.bookscout.data.local.UserSettingsRepository
import com.anjor.bookscout.data.remote.OpenLibraryApi
import com.anjor.bookscout.data.remote.dto.DocDto
import com.anjor.bookscout.data.remote.dto.SearchResponseDto
import com.anjor.bookscout.data.repository.BookRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    private class FakeSettings : UserSettingsRepository(mock(android.content.Context::class.java)) {
        override val settingsFlow = kotlinx.coroutines.flow.MutableStateFlow(UserSettings())
        override suspend fun addRecentSearch(query: String) = Unit // no-op, no DataStore needed
    }

    private class FakeApi(
        var searchResponse: SearchResponseDto? = null,
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

        override suspend fun getWork(workId: String): com.anjor.bookscout.data.remote.dto.WorkDto {
            return com.anjor.bookscout.data.remote.dto.WorkDto()
        }
    }

    private lateinit var api: FakeApi
    private lateinit var repository: BookRepository
    private lateinit var viewModel: SearchViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(dispatcher)
        api = FakeApi()
        repository = BookRepository(api, dispatcher)
        viewModel = SearchViewModel(repository, FakeSettings())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `blank query leaves the screen idle`() = runTest {
        viewModel.onQueryChange("   ")
        viewModel.search()
        assertEquals(SearchStatus.IDLE, viewModel.uiState.value.status)
    }

    @Test
    fun `successful search updates books and success status`() = runTest {
        api.searchResponse = SearchResponseDto(
            numFound = 1,
            start = 0,
            docs = listOf(DocDto(key = "/works/OL1", title = "Kotlin"))
        )
        viewModel.onQueryChange("kotlin")
        viewModel.search()

        val state = viewModel.uiState.value
        assertEquals(SearchStatus.SUCCESS, state.status)
        assertEquals(1, state.books.size)
        assertEquals("Kotlin", state.books.first().title)
    }

    @Test
    fun `failed search sets error status`() = runTest {
        api.error = java.io.IOException("offline")
        viewModel.search("kotlin")
        assertEquals(SearchStatus.ERROR, viewModel.uiState.value.status)
    }

    @Test
    fun `loadMore appends the next page`() = runTest {
        api.searchResponse = SearchResponseDto(
            numFound = 40,
            start = 0,
            docs = List(20) { DocDto(key = "/works/OL$it", title = "B$it", coverI = 1) }
        )
        viewModel.search("kotlin")
        assertEquals(20, viewModel.uiState.value.books.size)

        api.searchResponse = SearchResponseDto(
            numFound = 40,
            start = 20,
            docs = List(20) { DocDto(key = "/works/OLp2$it", title = "C$it", coverI = 1) }
        )
        viewModel.loadMore()
        assertEquals(40, viewModel.uiState.value.books.size)
        assertEquals(SearchStatus.SUCCESS, viewModel.uiState.value.status)
    }
}
