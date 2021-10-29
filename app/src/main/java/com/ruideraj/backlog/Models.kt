package com.ruideraj.backlog

import android.os.Parcelable
import androidx.room.*
import com.google.gson.annotations.SerializedName
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Constants.TABLE_NAME_LISTS
import com.ruideraj.backlog.data.ListIconConverters
import com.ruideraj.backlog.data.MediaTypeConverters
import com.ruideraj.backlog.data.MetadataConverters
import com.ruideraj.backlog.data.StatusConverters
import kotlinx.parcelize.Parcelize
import java.time.Year
import java.util.*

enum class MediaType { FILM, SHOW, GAME, BOOK }

enum class Status { TODO, IN_PROGRESS, DONE }

enum class ListIcon { LIST, FILM, SHOW, GAME, BOOK }

@Entity(tableName = TABLE_NAME_LISTS)
@Parcelize
data class BacklogList(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    @field:TypeConverters(ListIconConverters::class) val icon: ListIcon,
    val position: Double
) : Parcelable

@Entity(
    tableName = TABLE_NAME_ENTRIES, foreignKeys = [
        ForeignKey(
            entity = BacklogList::class,
            parentColumns = ["id"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["listId"])]
)
@Parcelize
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val listId: Long,
    val title: String,
    @field:TypeConverters(MediaTypeConverters::class) val type: MediaType,
    val position: Double,
    @field:TypeConverters(MetadataConverters::class) val metadata: Metadata,
    @field:TypeConverters(StatusConverters::class) val status: Status
) : Parcelable

sealed class Metadata : Parcelable {
    abstract val imageUrl: String?
    abstract val apiId: String?

    @Parcelize
    data class FilmData(
        val director: String? = null,
        val actors: String? = null,
        val releaseDate: Date? = null,
        override val imageUrl: String? = null,
        override val apiId: String? = null
    ) : Metadata()

    @Parcelize
    data class ShowData(
        val runDates: String? = null,
        override val imageUrl: String? = null,
        override val apiId: String? = null
    ) : Metadata()

    @Parcelize
    data class GameData(
        val developer: String? = null,
        val releaseDate: Date? = null,
        override val imageUrl: String? = null,
        override val apiId: String? = null
    ) : Metadata()

    @Parcelize
    data class BookData(
        val author: String? = null,
        //val publisher: String?,
        @SerializedName(value = "year_published") val yearPublished: Year? = null,
        override val imageUrl: String? = null,
        override val apiId: String? = null
    ) : Metadata()
}

@Parcelize
data class SearchResult(
    val type: MediaType,
    val title: String,
    val metadata: Metadata
) : Parcelable