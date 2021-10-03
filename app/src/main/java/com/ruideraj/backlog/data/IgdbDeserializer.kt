package com.ruideraj.backlog.data

import android.util.Log
import com.google.gson.*
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
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

            if (gameJson.get("name").asString.contains("solarus", ignoreCase = true)) {
                Log.d(TAG, "Solarus found")
            }

            val developersJson = gameJson.get("involved_companies")
            val developers = if (developersJson != null && developersJson !is JsonNull) {
                 developersJson.asJsonArray.joinToString { company ->
                     company.asJsonObject.get("company").asJsonObject.get("name").asString
                 }
            } else null

            var releaseDate: Date? = null
            val releaseDateJson = gameJson.get("first_release_date")
            if (releaseDateJson != null && releaseDateJson !is JsonNull) {
                releaseDate = Date(releaseDateJson.asLong * 1000)
            }

            var imageUrl: String? = null
            val coverJson = gameJson.get("cover")
            if (coverJson != null && coverJson !is JsonNull) {
                imageUrl = "https:" + coverJson.asJsonObject.get("url").asString
            }

            SearchResult(MediaType.GAME,
                gameJson.get("name").asString,
                Metadata.GameData(developers, releaseDate, imageUrl))
        }

        return IgdbResponse(jsonArray.size(), searchResults)
    }
}

data class IgdbResponse(val count: Int, val results: List<SearchResult>)