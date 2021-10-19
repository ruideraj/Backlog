package com.ruideraj.backlog.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.MoviesApi.Companion.TYPE_MOVIE
import com.ruideraj.backlog.data.MoviesApi.Companion.TYPE_SERIES
import java.lang.reflect.Type
import java.text.SimpleDateFormat

class MoviesDeserializer : JsonDeserializer<MoviesSearchResponse> {

    companion object {
        private val DATE_FORMAT = SimpleDateFormat("yyyy")
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : MoviesSearchResponse {
        val json = jsonElement as JsonObject

        val response = json.get("Response").asBoolean

        val count = if (response) json.get("totalResults").asInt else 0

        val searchResults = if (response) {
            json.getAsJsonArray("Search")
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
                    Metadata.FilmData(null , null, releaseYear, imageUrl, imdbId)
                } else {
                    val runDates = item.get("Year").asString
                    Metadata.ShowData(runDates, imageUrl, imdbId)
                }

                SearchResult(mediaType, item.get("Title").asString, metadata)
            }
        } else listOf()

        return MoviesSearchResponse(count, searchResults, response)
    }
}

data class MoviesSearchResponse(val totalResults: Int, val results: List<SearchResult>, val response: Boolean)