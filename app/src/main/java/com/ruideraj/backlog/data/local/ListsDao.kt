package com.ruideraj.backlog.data.local

import androidx.room.*
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants.TABLE_NAME_ENTRIES
import com.ruideraj.backlog.Constants.TABLE_NAME_LISTS
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.PositionTuple
import kotlinx.coroutines.flow.Flow

@Dao
interface ListsDao {

    @Query("SELECT *, (SELECT COUNT(entry.id) FROM $TABLE_NAME_ENTRIES entry WHERE listId = list.id) AS entries FROM $TABLE_NAME_LISTS list ORDER BY position")
    fun getAllLists(): Flow<List<ListItem>>

    @Query("SELECT COALESCE(MAX(position), 0) FROM $TABLE_NAME_LISTS")
    suspend fun getMaxPosition(): Double

    @Insert
    suspend fun insertList(newList: BacklogList)

    @Query("DELETE FROM $TABLE_NAME_LISTS WHERE id = :listId")
    suspend fun deleteList(listId: Long)

    @Query("UPDATE $TABLE_NAME_LISTS SET title = :title, icon = :icon WHERE id = :listId")
    @TypeConverters(ListIconConverters::class)
    suspend fun updateListDetails(listId: Long, title: String, icon: ListIcon)

    @Query("UPDATE $TABLE_NAME_LISTS SET position = :newPosition WHERE id = :listId")
    suspend fun updateListPosition(listId: Long, newPosition: Double)

    @Query("SELECT id, position FROM $TABLE_NAME_LISTS ORDER BY position")
    suspend fun getAllListPositions(): List<PositionTuple>

}

data class ListItem(@Embedded val list: BacklogList, val entries: Int)