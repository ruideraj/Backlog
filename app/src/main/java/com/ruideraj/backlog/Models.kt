package com.ruideraj.backlog

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.ruideraj.backlog.Constants.TABLE_NAME_LISTS
import com.ruideraj.backlog.data.ListIconConverters
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

data class Entry(val id: Long,
                 val listId: Long,
                 val position: Double,
                 val type: MediaType,
                 val title: String,
                 val metadata: Metadata,
                 val status: Status)

sealed class Metadata {
    class FilmData(val director: String, releaseDate: Date) : Metadata()
    class GameData(val developer: String, releaseDate: Date) : Metadata()
    class ShowData(val showrunner: String, releaseDate: Date) : Metadata()
    class BookData(val author: String, releaseDate: Date) : Metadata()
}
