package com.ruideraj.backlog.lists

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

interface ListsRepository {
    fun loadLists(): Flow<List<BacklogList>>
    suspend fun createList(title: String, icon: ListIcon)
    suspend fun editList(listId: Long, title: String, icon: ListIcon)
    suspend fun deleteList(listId: Long)
}

@Singleton
class FakeListsRepository @Inject constructor() : ListsRepository {
    private val lists = mutableListOf<BacklogList>().apply {
        val icons = ListIcon.values()
        val iconTypes = icons.size
        for (i in 0..18) {
            val iconType = icons[i % iconTypes]
            add(BacklogList(i.toLong(), "list$i", iconType, i, i))
        }
    }

    private val listsFlow = MutableStateFlow(lists)

    override fun loadLists(): Flow<List<BacklogList>> = listsFlow

    override suspend fun createList(title: String, icon: ListIcon) {
        val newId = lists.size
        val newList = BacklogList(newId.toLong(), title , icon, newId, newId)
        lists.add(newList)
        listsFlow.value = lists
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteList(listId: Long) {
        TODO("Not yet implemented")
    }
}