package com.ruideraj.backlog.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.search.MOVIES_PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

class MoviesPagingSource(private val moviesApi: MoviesApi,
                         private val type: MediaType,
                         private val query: String) : PagingSource<Int, SearchResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        return try {
            val page = params.key ?: 1

            val apiType = if (type == MediaType.FILM) MoviesApi.TYPE_MOVIE else MoviesApi.TYPE_SERIES

            val response = moviesApi.searchTitles(query, apiType, page)
            val prevKey = if (page == 1) null else page - 1

            if (response is MoviesSearchResponse.Error) {
                if (response.message.contains("not found")) {
                    LoadResult.Page(listOf(), prevKey, null)
                } else {
                    LoadResult.Error(ApiException(response.message))
                }
            } else {
                val success = response as MoviesSearchResponse.Success

                val searchResults = success.results
                val nextKey = if (searchResults.isEmpty()) {
                    null
                } else {
                    // Initial load size may be larger than normal page size, due to PagingConfig.initialLoadSize
                    // so calculate next page based on current load size
                    page + (params.loadSize / MOVIES_PAGE_SIZE)
                }

                LoadResult.Page(searchResults, prevKey, nextKey)
            }
        } catch (e: IOException) {
            LoadResult.Error(e)
        } catch (e: HttpException) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, SearchResult>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            state.closestPageToPosition(anchorPosition)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchorPosition)?.nextKey?.minus(1)
        }
    }
}