package com.ruideraj.backlog.data.remote

import com.ruideraj.backlog.data.remote.OpenLibraryResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenLibraryApi {

    @GET("search.json?fields=title,author_name,first_publish_year,cover_i")
    suspend fun search(@Query("title") title: String,
                       @Query("limit") limit: Int? = null,
                       @Query("offset") offset: Int = 0): OpenLibraryResponse

}