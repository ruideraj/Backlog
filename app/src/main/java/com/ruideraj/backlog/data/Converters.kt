package com.ruideraj.backlog.data

import androidx.room.TypeConverter
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Status

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