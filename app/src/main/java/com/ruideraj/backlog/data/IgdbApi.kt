package com.ruideraj.backlog.data

import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IgdbApi {

    @POST("games")
    suspend fun searchGames(@Body requestBody: RequestBody): IgdbResponse

}