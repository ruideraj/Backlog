package com.ruideraj.backlog.data

import androidx.room.TypeConverter
import com.ruideraj.backlog.ListIcon

class ListIconConverters {
    @TypeConverter
    fun listIconToInt(icon: ListIcon): Int = icon.ordinal

    @TypeConverter
    fun iconIntToListIcon(iconInt: Int)  = ListIcon.values()[iconInt]
}