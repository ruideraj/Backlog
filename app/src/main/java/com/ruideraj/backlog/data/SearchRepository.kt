package com.ruideraj.backlog.data

import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface SearchRepository {
    suspend fun searchByTitle(type: MediaType, query: String, pageSize: Int, page: Int = 0): List<SearchResult>
}

class SearchRepositoryImpl @Inject constructor(
    private val openLibraryApi: OpenLibraryApi
) : SearchRepository {

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
}