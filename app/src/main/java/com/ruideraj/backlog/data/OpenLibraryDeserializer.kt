package com.ruideraj.backlog.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.SearchResult
import java.lang.reflect.Type
import java.time.Year

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
            val authors = work.get("author_name")?.let {
                it.asJsonArray.joinToString { author -> author.asString }
            }
            val yearPublished = work.get("first_publish_year")?.let { Year.of(it.asInt) }
            val imageUrl = work.get("cover_i")?.let { "https://covers.openlibrary.org/b/id/${it.asInt}-S.jpg" }

            val bookData = Metadata.BookData(authors, yearPublished, imageUrl)

            SearchResult(MediaType.BOOK, work.get("title").asString, bookData)
        }

        return OpenLibraryResponse(json.get("numFound").asInt, json.get("start").asInt, results)
    }
}

data class OpenLibraryResponse(val numFound: Int, val start: Int, val docs: List<SearchResult>)