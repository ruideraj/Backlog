package com.ruideraj.backlog.data.local

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.google.gson.*
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.Status
import java.lang.reflect.Type
import java.time.Year

class ListIconConverters {
    @TypeConverter
    fun listIconToInt(icon: ListIcon) = icon.ordinal

    @TypeConverter
    fun iconIntToListIcon(iconInt: Int) = ListIcon.values()[iconInt]
}

class MediaTypeConverters {
    @TypeConverter
    fun mediaTypeToInt(type: MediaType) = type.ordinal

    @TypeConverter
    fun typeIntToMediaType(typeInt: Int) = MediaType.values()[typeInt]
}

class StatusConverters {
    @TypeConverter
    fun statusToInt(status: Status) = status.ordinal

    @TypeConverter
    fun intToStatus(statusInt: Int) = Status.values()[statusInt]
}

@ProvidedTypeConverter
class MetadataConverters(private val gson: Gson) {

    companion object {
        private const val PROP_TYPE = "type"

        private val metadataToIntMap = mapOf(
            Metadata.FilmData::class to 0,
            Metadata.ShowData::class to 1,
            Metadata.GameData::class to 2,
            Metadata.BookData::class to 3
        )
        private val intToMetadataMap = mapOf(
            0 to Metadata.FilmData::class,
            1 to Metadata.ShowData::class,
            2 to Metadata.GameData::class,
            3 to Metadata.BookData::class
        )
    }

    @TypeConverter
    fun metadataToString(metadata: Metadata) : String {
        val jsonElement = gson.toJsonTree(metadata)
        val typeInt = metadataToIntMap[metadata::class]
        jsonElement.asJsonObject.addProperty(PROP_TYPE, typeInt)

        return gson.toJson(jsonElement)
    }

    @TypeConverter
    fun stringToMetadata(string: String) : Metadata {
        val jsonObject = gson.fromJson(string, JsonObject::class.java)
        val typeInt = jsonObject.get(PROP_TYPE)
            ?: throw IllegalStateException("Stored Metadata should have an int to indicate type")
        val metadataClass = intToMetadataMap[typeInt.asInt]
            ?: throw IllegalArgumentException("Invalid int for Metadata type")

        return gson.fromJson(jsonObject, metadataClass.java)
    }
}

class YearConverter : JsonSerializer<Year>, JsonDeserializer<Year> {
    override fun serialize(src: Year, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.toString())
    }

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Year {
        return Year.of(json.asInt)
    }
}