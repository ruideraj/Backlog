package com.ruideraj.backlog.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.search.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

interface SearchRepository {
    fun getTitleSearchStream(type: MediaType, query: String): Flow<PagingData<SearchResult>>
}

class SearchRepositoryImpl @Inject constructor(
    private val openLibraryApi: OpenLibraryApi,
    private val igdbApi: IgdbApi)
    : SearchRepository {
    companion object {
        private const val TAG = "SearchRepositoryImpl"
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
                    pagingSourceFactory = { IgdbPagingSource(igdbApi, query) }
                ).flow
            }
            else -> flowOf()
        }
    }
}