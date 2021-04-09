package com.ruideraj.backlog.lists

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ListsRepository {
    fun loadLists(): Flow<List<BacklogList>>
    suspend fun createList(title: String, icon: ListIcon)
    suspend fun editList(listId: Long, title: String, icon: ListIcon)
    suspend fun deleteList(listId: Long)
}

class ListsRepositoryImpl @Inject constructor(private val listsDao: ListsDao,
                                              @IoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : ListsRepository {

    override fun loadLists(): Flow<List<BacklogList>> = listsDao.getAllLists()

    override suspend fun createList(title: String, icon: ListIcon) {
        withContext(ioDispatcher) {
            val position = listsDao.getMaxPosition() + 1
            val newList = BacklogList(0, title, icon, position, 0) // id automatically set by Room
            listsDao.insertList(newList)
        }
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        withContext(ioDispatcher) {
            listsDao.updateList(listId, title, icon)
        }
    }

    override suspend fun deleteList(listId: Long) {
        withContext(ioDispatcher) {
            listsDao.deleteList(listId)
        }
    }
}