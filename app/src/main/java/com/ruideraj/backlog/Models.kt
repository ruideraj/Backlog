package com.ruideraj.backlog

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

enum class MediaType { FILM, TV, GAME, BOOK }

enum class Status { TODO, IN_PROGRESS, DONE }

enum class ListIcon { LIST, FILM, TV, GAME, BOOK }

@Parcelize
data class BacklogList(val listId: Long,
                       val title: String,
                       val icon: ListIcon,
                       val position: Int,
                       val count: Int) : Parcelable

data class Entry(val entryId: Long,
                 val listId: Long,
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
