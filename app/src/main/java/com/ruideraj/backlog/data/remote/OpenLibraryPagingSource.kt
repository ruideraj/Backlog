package com.ruideraj.backlog.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.search.PAGE_SIZE
import retrofit2.HttpException
import java.io.IOException

class OpenLibraryPagingSource(private val openLibraryApi: OpenLibraryApi,
                              private val query: String) : PagingSource<Int, SearchResult>() {
    companion object {
        private const val TAG = "OpenLibraryPagingSource"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize
            val offset = page * pageSize
            val response = openLibraryApi.search(query, pageSize, offset)
            val searchResults = response.docs

            val prevKey = if (page == 0) null else page - 1
            val nextKey = if (searchResults.isEmpty()) {
                null
            } else {
                // Initial load size may be larger than normal page size, due to PagingConfig.initialLoadSize
                // so calculate next page based on current load size
                page + (params.loadSize / PAGE_SIZE)
            }

            LoadResult.Page(searchResults, prevKey, nextKey)
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