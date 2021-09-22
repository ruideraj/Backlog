package com.ruideraj.backlog.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.search.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface SearchRepository {
    suspend fun searchByTitle(type: MediaType, query: String, pageSize: Int, page: Int = 0): List<SearchResult>
    fun getTitleSearchStream(type: MediaType, query: String): Flow<PagingData<SearchResult>>
}

class SearchRepositoryImpl @Inject constructor(
    private val openLibraryApi: OpenLibraryApi,
    private val rawgApi: RawgApi,
    private val propertiesReader: PropertiesReader
) : SearchRepository {
    companion object {
        private const val TAG = "SearchRepositoryImpl"
    }

    override suspend fun searchByTitle(type: MediaType, query: String, pageSize: Int, page: Int)
    : List<SearchResult> {

        val testMetadata = when (type) {
            MediaType.FILM -> Metadata.FilmData(null, null, null)
            MediaType.SHOW -> Metadata.ShowData(null, null)
            MediaType.GAME -> Metadata.GameData(null, null, null)
            MediaType.BOOK -> Metadata.BookData(null, null, null)
        }

        return when (type) {
            MediaType.BOOK -> openLibraryApi.search(query, pageSize, page * pageSize).docs
            else -> if ("sample title".contains(query, true)) {
                listOf(
                    SearchResult(type, "sample title 1", testMetadata),
                    SearchResult(type, "sample title 2", testMetadata)
                )
            } else {
                listOf()
            }
        }
    }

    override fun getTitleSearchStream(type: MediaType, query: String): Flow<PagingData<SearchResult>> {
        return when (type) {
            MediaType.BOOK -> {
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { OpenLibraryPagingSource(openLibraryApi, query) }
                ).flow
            }
            MediaType.GAME -> {
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { RawgPagingSource(rawgApi, propertiesReader, query) }
                ).flow
            }
            else -> flowOf()
        }
    }
}