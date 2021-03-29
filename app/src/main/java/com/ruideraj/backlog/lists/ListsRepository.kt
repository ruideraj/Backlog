package com.ruideraj.backlog.lists

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.AppDatabase
import com.ruideraj.backlog.injection.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import javax.inject.Inject

interface ListsRepository {
    fun loadLists(): Flow<List<BacklogList>>
    suspend fun createList(title: String, icon: ListIcon)
    suspend fun editList(listId: Long, title: String, icon: ListIcon)
    suspend fun deleteList(listId: Long)
}

class ListsRepositoryImpl @Inject constructor(private val appDatabase: AppDatabase,
                                              @IoDispatcher private val ioDispatcher: CoroutineDispatcher)
    : ListsRepository {
    private var listCount = 0

    override fun loadLists(): Flow<List<BacklogList>> = appDatabase.listsDao().getAllLists().onEach { lists ->
        listCount = lists.size
    }

    override suspend fun createList(title: String, icon: ListIcon) {
        val newList = BacklogList(0, title, icon, listCount, 0)

        withContext(ioDispatcher) {
            appDatabase.listsDao().insertList(newList)
        }
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        withContext(ioDispatcher) {
            appDatabase.listsDao().updateList(listId, title, icon)
        }
    }

    override suspend fun deleteList(listId: Long) {
        withContext(ioDispatcher) {
            appDatabase.listsDao().deleteList(listId)
        }
    }
}