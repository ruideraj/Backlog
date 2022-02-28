package com.ruideraj.backlog.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.data.local.EntriesDao
import com.ruideraj.backlog.data.local.ListsDao

@Database(entities = [BacklogList::class, Entry::class], version = 1)
abstract class AppDatabase : RoomDatabase() {

    abstract fun listsDao(): ListsDao
    abstract fun entriesDao(): EntriesDao

}