package com.ruideraj.backlog.data

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.remote.*
import com.ruideraj.backlog.data.remote.MoviesApi.Companion.TYPE_MOVIE
import com.ruideraj.backlog.data.remote.MoviesApi.Companion.TYPE_SERIES
import com.ruideraj.backlog.search.MOVIES_PAGE_SIZE
import com.ruideraj.backlog.search.PAGE_SIZE
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface SearchRepository {
    fun getTitleSearchStream(type: MediaType, query: String): Flow<PagingData<SearchResult>>
    suspend fun getDetails(inputResult: SearchResult): SearchResult
}

class SearchRepositoryImpl @Inject constructor(
    private val moviesApi: MoviesApi,
    private val igdbApi: IgdbApi,
    private val openLibraryApi: OpenLibraryApi
) : SearchRepository {
    companion object {
        private const val TAG = "SearchRepositoryImpl"
    }

    override fun getTitleSearchStream(type: MediaType, query: String): Flow<PagingData<SearchResult>> {
        return when (type) {
            MediaType.FILM, MediaType.SHOW -> {
                Pager(
                    config = PagingConfig(
                        pageSize =  MOVIES_PAGE_SIZE,
                        enablePlaceholders = false,
                        initialLoadSize = MOVIES_PAGE_SIZE
                    ),
                    pagingSourceFactory = { MoviesPagingSource(moviesApi, type, query) }
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
            MediaType.BOOK -> {
                Pager(
                    config = PagingConfig(
                        pageSize = PAGE_SIZE,
                        enablePlaceholders = false
                    ),
                    pagingSourceFactory = { OpenLibraryPagingSource(openLibraryApi, query) }
                ).flow
            }
        }
    }

    override suspend fun getDetails(inputResult: SearchResult): SearchResult {
        val mediaType = inputResult.type

        if (!(mediaType == MediaType.FILM || mediaType == MediaType.SHOW)) {
            throw IllegalArgumentException("Invalid types for Movies API")
        }

        val type = if (mediaType == MediaType.FILM) TYPE_MOVIE else TYPE_SERIES
        val apiId = inputResult.metadata.apiId
            ?: throw IllegalArgumentException("Must have API Id to fetch details")

        val response = moviesApi.getDetailsById(type, apiId)

        if (response is MoviesDetailsResponse.Error) {
            throw ApiException(response.message)
        } else {
            return (response as MoviesDetailsResponse.Success).searchResult
        }
    }
}