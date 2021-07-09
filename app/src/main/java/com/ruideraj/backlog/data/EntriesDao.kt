package com.ruideraj.backlog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.TypeConverters
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.Status
import kotlinx.coroutines.flow.Flow

@Dao
interface EntriesDao {

    @Query("SELECT * FROM $TABLE_NAME_ENTRIES WHERE listId = :listId ORDER BY position")
    fun getEntriesForList(listId: Long): Flow<List<Entry>>

    @Query("SELECT MAX(position) FROM $TABLE_NAME_ENTRIES WHERE listId = :listId")
    fun getMaxPositionForList(listId: Long): Double

    @Insert
    fun insertEntry(entry: Entry)

    @Query("UPDATE $TABLE_NAME_ENTRIES SET title = :title, metadata = :metadata WHERE id = :entryId")
    @TypeConverters(MetadataConverters::class)
    fun updateEntry(entryId: Long, title: String, metadata: Metadata)

    @Query("UPDATE $TABLE_NAME_ENTRIES SET status = :status WHERE id = :entryId")
    @TypeConverters(StatusConverters::class)
    fun updateEntryStatus(entryId: Long, status: Status)

    @Query("UPDATE $TABLE_NAME_ENTRIES SET position = :position WHERE id = :entryId")
    fun updateEntryPosition(entryId: Long, position: Double)

    @Query("SELECT listId FROM $TABLE_NAME_ENTRIES WHERE id = :entryId")
    fun getListIdForEntry(entryId: Long): Long

    @Query("SELECT id, position FROM $TABLE_NAME_ENTRIES WHERE listId = :listId ORDER BY position")
    fun getAllEntryPositionsForList(listId: Long): List<PositionTuple>

    @Query("DELETE FROM $TABLE_NAME_ENTRIES WHERE id IN (:ids)")
    fun deleteEntries(ids: List<Long>)

}
