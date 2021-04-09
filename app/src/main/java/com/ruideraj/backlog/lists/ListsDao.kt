package com.ruideraj.backlog.lists

import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.Constants.TABLE_NAME_LISTS
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.ListIconConverters
import kotlinx.coroutines.flow.Flow

@Dao
interface ListsDao {

    @Query("SELECT * FROM $TABLE_NAME_LISTS ORDER BY position")
    fun getAllLists(): Flow<List<BacklogList>>

    @Query("SELECT MAX(position) FROM $TABLE_NAME_LISTS")
    fun getMaxPosition(): Double

    @Query("SELECT position FROM $TABLE_NAME_LISTS WHERE id = :listId")
    fun getListPositionFromId(listId: Long): Double

    @Insert
    fun insertList(newList: BacklogList)

    @Query("DELETE FROM $TABLE_NAME_LISTS WHERE id = :listId")
    fun deleteList(listId: Long)

    @Query("UPDATE $TABLE_NAME_LISTS SET title = :title, icon = :icon WHERE id = :listId")
    @TypeConverters(ListIconConverters::class)
    fun updateList(listId: Long, title: String, icon: ListIcon)

}