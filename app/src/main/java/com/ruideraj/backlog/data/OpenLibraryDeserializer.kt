package com.ruideraj.backlog.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import java.lang.reflect.Type

class OpenLibraryDeserializer : JsonDeserializer<OpenLibraryResponse> {
    companion object {
        private const val TAG = "OpenLibraryDeserializer"
    }

    override fun deserialize(jsonElement: JsonElement, typeOfT: Type, context: JsonDeserializationContext)
    : OpenLibraryResponse {
        val json = jsonElement as JsonObject

        val docs = json.getAsJsonArray("docs")
        val results = docs.map { doc ->
            val work = doc.asJsonObject
            val authors = work.get("author_name").asJsonArray.joinToString { it.asString }
            val firstYearPublished = work.get("first_publish_year").asInt

            // TODO Parse publish date/year and publisher(?)
            val bookData = Metadata.BookData(authors, null, null)

            SearchResult(MediaType.BOOK, work.get("title").asString, bookData)
        }

        return OpenLibraryResponse(json.get("numFound").asInt, json.get("start").asInt, results)
    }
}

data class OpenLibraryResponse(val numFound: Int, val start: Int, val docs: List<SearchResult>)