package com.ruideraj.backlog.data

import retrofit2.http.GET
import retrofit2.http.Query

interface RawgApi {

    @GET("games")
    suspend fun search(@Query("key") key: String,
                       @Query("search") search: String,
                       @Query("page_size") pageSize: Int,
                       @Query("page") page: Int = 1): RawgResponse

}