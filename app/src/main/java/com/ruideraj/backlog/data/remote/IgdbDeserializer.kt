package com.ruideraj.backlog.data.remote

import com.google.gson.*
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.parseIfNotNull
import java.lang.reflect.Type
import java.util.*

class IgdbDeserializer : JsonDeserializer<IgdbResponse> {

    companion object {
        private const val TAG = "IgdbDeserializer"
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : IgdbResponse {
        val jsonArray = jsonElement as JsonArray

        val searchResults = jsonArray.map { element ->
            val gameJson = element.asJsonObject

            val developers = gameJson.parseIfNotNull("involved_companies") {
                it.asJsonArray.joinToString { company ->
                    company.asJsonObject.get("company").asJsonObject.get("name").asString
                }
            }
            val releaseDate = gameJson.parseIfNotNull("first_release_date") { Date(it.asLong * 1000) }
            val imageUrl = gameJson.parseIfNotNull("cover") { "https:" + it.asJsonObject.get("url").asString }

            SearchResult(MediaType.GAME,
                gameJson.get("name").asString,
                Metadata.GameData(developers, releaseDate, imageUrl))
        }

        return IgdbResponse(jsonArray.size(), searchResults)
    }
}

data class IgdbResponse(val count: Int, val results: List<SearchResult>)