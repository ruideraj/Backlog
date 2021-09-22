package com.ruideraj.backlog.data

import com.google.gson.*
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import java.lang.reflect.Type
import java.text.ParseException
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class RawgDeserializer : JsonDeserializer<RawgResponse> {
    companion object {
        private const val TAG = "RawgDeserializer"
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : RawgResponse {
        val json = jsonElement as JsonObject

        val results = json.getAsJsonArray("results")

        val searchResults = results.map { element ->
            val gameJson = element.asJsonObject

            var releaseDate: Date? = null
            val releaseDateJson = gameJson.get("released")
            if (releaseDateJson != null && releaseDateJson !is JsonNull) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd")
                try {
                    releaseDate = dateFormat.parse(releaseDateJson.asString)
                } catch (e: ParseException) {
                    e.printStackTrace()
                }
            }

            SearchResult(MediaType.GAME, gameJson.get("name").asString, Metadata.GameData(null, releaseDate, null))
        }

        return RawgResponse(json.get("count").asInt, searchResults)
    }
}

data class RawgResponse(val count: Int, val results: List<SearchResult>)