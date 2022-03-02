package com.ruideraj.backlog.data

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.local.ListItem
import com.ruideraj.backlog.data.local.ListsDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

interface ListsRepository {
    fun loadLists(): Flow<List<ListItem>>
    suspend fun createList(title: String, icon: ListIcon)
    suspend fun editList(listId: Long, title: String, icon: ListIcon)
    suspend fun deleteList(listId: Long)
    suspend fun moveList(listId: Long, newPosition: Int)
}

class ListsRepositoryImpl @Inject constructor(private val listsDao: ListsDao)
    : ListsRepository {

    override fun loadLists(): Flow<List<ListItem>> = listsDao.getAllLists()

    override suspend fun createList(title: String, icon: ListIcon) {
        val position = listsDao.getMaxPosition() + 1
        val newList = BacklogList(0, title, icon, position) // id automatically set by Room
        listsDao.insertList(newList)
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        listsDao.updateListDetails(listId, title, icon)
    }

    override suspend fun deleteList(listId: Long) {
        listsDao.deleteList(listId)
    }

    override suspend fun moveList(listId: Long, newPosition: Int) {
        val listPositions = listsDao.getAllListPositions()
        val newPositionValue = findNewPositionValue(listPositions, listId, newPosition)
        listsDao.updateListPosition(listId, newPositionValue)
    }
}