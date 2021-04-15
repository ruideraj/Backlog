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
    suspend fun moveList(listId: Long, newPosition: Int)
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
            listsDao.updateListDetails(listId, title, icon)
        }
    }

    override suspend fun deleteList(listId: Long) {
        withContext(ioDispatcher) {
            listsDao.deleteList(listId)
        }
    }

    override suspend fun moveList(listId: Long, newPosition: Int) {
        withContext(ioDispatcher) {
            val listPositions = listsDao.getAllListPositions()
            val currentPosition = listPositions.indexOfFirst { it.id == listId }

            val newPositionValue = when (newPosition) {
                0 -> listPositions[0].position - 1
                listPositions.size - 1 -> listPositions[listPositions.size - 1].position + 1
                else -> {
                    val positionA = listPositions[newPosition].position
                    val positionB = if (newPosition > currentPosition) {
                        listPositions[newPosition + 1].position
                    } else {
                        listPositions[newPosition - 1].position
                    }

                    (positionA + positionB) / 2
                }
            }

            listsDao.updateListPosition(listId, newPositionValue)
        }
    }
}