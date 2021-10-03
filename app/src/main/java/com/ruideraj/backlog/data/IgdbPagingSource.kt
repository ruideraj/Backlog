package com.ruideraj.backlog.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.search.PAGE_SIZE
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.HttpException
import java.io.IOException

class IgdbPagingSource(private val igdbApi: IgdbApi,
                       private val query: String) : PagingSource<Int, SearchResult>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchResult> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            val fields = "fields name,involved_companies.company.name,first_release_date,cover.url;"
            val search = "search \"$query\";"
            val limit = "limit $pageSize;"
            val offset ="offset ${page * pageSize};"

            val bodyText = fields + search + limit + offset
            val requestBody = bodyText.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = igdbApi.searchGames(requestBody)
            val searchResults = response.results

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