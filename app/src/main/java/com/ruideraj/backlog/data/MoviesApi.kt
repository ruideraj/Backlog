package com.ruideraj.backlog.data

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * API from RapidApi that covers both movies and tv series.
 */
interface MoviesApi {

    companion object {
        const val TYPE_MOVIE = "movie"
        const val TYPE_SERIES = "series"
    }

    @GET("?r=json")
    suspend fun searchTitles(@Query("s") s: String,
                             @Query("type") type: String,
                             @Query("page") page: Int = 1): MoviesSearchResponse

    @GET("?r=json")
    suspend fun getDetailsById(@Query("type") type: String, @Query("i") id: String): MoviesDetailsResponse

}