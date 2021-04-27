package com.ruideraj.backlog

import android.os.Parcelable
import androidx.room.*
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Constants.TABLE_NAME_LISTS
import com.ruideraj.backlog.data.ListIconConverters
import com.ruideraj.backlog.data.MediaTypeConverters
import com.ruideraj.backlog.data.StatusConverters
import kotlinx.parcelize.Parcelize
import java.util.*

enum class MediaType { FILM, TV, GAME, BOOK }

enum class Status { TODO, IN_PROGRESS, DONE }

enum class ListIcon { LIST, FILM, TV, GAME, BOOK }

@Entity(tableName = TABLE_NAME_LISTS)
@Parcelize
data class BacklogList (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String,
    @field:TypeConverters(ListIconConverters::class) val icon: ListIcon,
    val position: Double,
    val count: Int) : Parcelable

@Entity(tableName = TABLE_NAME_ENTRIES, foreignKeys = [
    ForeignKey(entity = BacklogList::class,
        parentColumns = ["id"],
        childColumns = ["listId"],
        onDelete = ForeignKey.CASCADE)],
    indices = [Index(value = ["listId"])])
@Parcelize
data class Entry(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val listId: Long,
    val title: String,
    @field:TypeConverters(MediaTypeConverters::class) val type: MediaType,
    val position: Double,
    // TODO val metadata: Metadata,
    @field:TypeConverters(StatusConverters::class) val status: Status) : Parcelable

sealed class Metadata {
    class FilmData(val director: String, releaseDate: Date) : Metadata()
    class GameData(val developer: String, releaseDate: Date) : Metadata()
    class ShowData(val showrunner: String, releaseDate: Date) : Metadata()
    class BookData(val author: String, releaseDate: Date) : Metadata()
}
