package com.ruideraj.backlog.data

import com.google.gson.*
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.MoviesApi.Companion.TYPE_MOVIE
import com.ruideraj.backlog.data.MoviesApi.Companion.TYPE_SERIES
import java.lang.reflect.Type
import java.text.SimpleDateFormat
import java.util.*

class MoviesSearchDeserializer : JsonDeserializer<MoviesSearchResponse> {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy")
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : MoviesSearchResponse {
        val json = jsonElement as JsonObject

        val response = json.get("Response").asBoolean

        return if (response) {
            val totalResults = if (response) json.get("totalResults").asInt else 0
            val results = json.getAsJsonArray("Search")
                .map { element -> element.asJsonObject }
                .filter { jsonObject ->
                    val type = jsonObject.get("Type").asString
                    type == TYPE_MOVIE || type == TYPE_SERIES
                }.map { item ->
                    val type = item.get("Type").asString
                    val mediaType = if (type == TYPE_MOVIE) MediaType.FILM else MediaType.SHOW
                    val imdbId = item.get("imdbID").asString

                    val imageUrl = item.get("Poster").asString

                    val metadata = if (mediaType == MediaType.FILM) {
                        val releaseYear = DATE_FORMAT.parse(item.get("Year").asString)
                        Metadata.FilmData(null, null, releaseYear, imageUrl, imdbId)
                    } else {
                        val runDates = item.get("Year").asString
                        Metadata.ShowData(runDates, imageUrl, imdbId)
                    }

                    SearchResult(mediaType, item.get("Title").asString, metadata)
                }
            MoviesSearchResponse.Success(totalResults, results)
        } else {
            val message = json.get("Error").asString
            MoviesSearchResponse.Error(message)
        }
    }
}

sealed class MoviesSearchResponse {
    data class Success(val totalResults: Int, val results: List<SearchResult>) : MoviesSearchResponse()
    data class Error(val message: String) : MoviesSearchResponse()
}

class MoviesDetailsDeserializer : JsonDeserializer<MoviesDetailsResponse> {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("dd MMM yyyy")
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : MoviesDetailsResponse {
        val json = jsonElement as JsonObject

        val response = json.get("Response").asBoolean

        var searchResult: SearchResult? = null
        var error: String? = null
        if (response) {
            val title = json.get("Title").asString
            val type = json.get("Type").asString
            val mediaType = if (type == TYPE_MOVIE) MediaType.FILM else MediaType.SHOW

            val imageUrl = json.parseIfNotNull("Poster") { it.asString }
            val imdbID = json.parseIfNotNull("imdbID") { it.asString }

            val metadata = if (mediaType == MediaType.FILM) {
                val director = json.parseIfNotNull("Director") { it.asString }
                val actors = json.parseIfNotNull("Actors") { it.asString }
                val releaseDate = json.parseIfNotNull("Released") { DATE_FORMAT.parse(it.asString) }
                Metadata.FilmData(director, actors, releaseDate, imageUrl, imdbID)
            } else {
                val runDates = json.parseIfNotNull("Year") { it.asString }
                Metadata.ShowData(runDates, imageUrl, imdbID)
            }

            SearchResult(mediaType, title, metadata)
        } else {
            error = json.parseIfNotNull("Error") { it.asString }
        }

        return MoviesDetailsResponse(searchResult, error, response)
    }
}

data class MoviesDetailsResponse(val searchResult: SearchResult?, val error: String?, val response: Boolean)