package com.ruideraj.backlog.lists

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
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

    private val listsFlow = MutableStateFlow<List<BacklogList>>(lists)

    override fun loadLists(): Flow<List<BacklogList>> = listsFlow

    override suspend fun createList(title: String, icon: ListIcon) {
        delay(1000)
        val currentMainList = listsFlow.value
        val newId = currentMainList.size
        val newList = BacklogList(newId.toLong(), title , icon, newId, newId)
        val newMainList = currentMainList + newList
        listsFlow.value = newMainList
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        delay(1000)
        val lists = listsFlow.value
        val indexToEdit = lists.indexOfFirst { it.listId == listId }
        val editedList = lists[indexToEdit].copy(title = title, icon = icon)
        listsFlow.value = lists.toMutableList().apply { this[indexToEdit] = editedList }
    }

    override suspend fun deleteList(listId: Long) {
        delay(1000)
        listsFlow.value = listsFlow.value.filter { it.listId != listId }
    }
}