package com.ruideraj.backlog.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.lists.ListsDao

@Database(entities = [BacklogList::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listsDao(): ListsDao

}