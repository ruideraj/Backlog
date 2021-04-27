package com.ruideraj.backlog.data

import androidx.room.Dao
import androidx.room.Query
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Entry
import kotlinx.coroutines.flow.Flow

@Dao
interface EntriesDao {

    @Query("SELECT * FROM $TABLE_NAME_ENTRIES WHERE listId = :listId")
    fun getEntriesForList(listId: Long): Flow<List<Entry>>

}
