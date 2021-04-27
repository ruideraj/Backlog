package com.ruideraj.backlog.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Entry

@Database(entities = [BacklogList::class, Entry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listsDao(): ListsDao
    abstract fun entriesDao(): EntriesDao

}