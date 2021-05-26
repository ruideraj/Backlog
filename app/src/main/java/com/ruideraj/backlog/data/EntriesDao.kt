package com.ruideraj.backlog.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.TypeConverters
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface EntriesDao {

    @Query("SELECT * FROM $TABLE_NAME_ENTRIES WHERE listId = :listId")
    fun getEntriesForList(listId: Long): Flow<List<Entry>>

    @Query("UPDATE $TABLE_NAME_ENTRIES SET status = :status WHERE id = :entryId")
    @TypeConverters(StatusConverters::class)
    fun updateEntryStatus(entryId: Long, status: Status)

}