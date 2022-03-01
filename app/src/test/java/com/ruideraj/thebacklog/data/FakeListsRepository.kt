package com.ruideraj.thebacklog.data

import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.data.ListsRepository
import com.ruideraj.backlog.data.local.ListItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeListsRepository(lists: List<ListItem> = emptyList()) : ListsRepository {
    private val listsFlow = MutableStateFlow(lists)

    override fun loadLists(): Flow<List<ListItem>> = listsFlow

    override suspend fun createList(title: String, icon: ListIcon) {
        val currentLists = listsFlow.value
        val newId = currentLists.maxOf { it.list.id } + 1
        val newPosition = currentLists.maxOf { it.list.position } + 1
        val newList = BacklogList(newId, title, icon, newPosition)

        listsFlow.value = currentLists + ListItem(newList, 0)
    }

    override suspend fun editList(listId: Long, title: String, icon: ListIcon) {
        listsFlow.value.firstOrNull { it.list.id == listId }?.let {
            val newList = it.list.copy(title = title, icon = icon)

            val currentLists = listsFlow.value
            val updatedLists = currentLists.map {
                    listItem -> if (listItem.list.id == listId) ListItem(newList, it.entries) else listItem
            }
            listsFlow.value = updatedLists
        }
    }

    override suspend fun deleteList(listId: Long) {
        val currentLists = listsFlow.value
        listsFlow.value = currentLists.filter { it.list.id != listId }
    }

    override suspend fun moveList(listId: Long, newPosition: Int) {
        TODO("Not yet implemented")
    }

    fun addLists(lists: List<ListItem>) {
        val newLists = listsFlow.value + lists
        listsFlow.value = newLists
    }
}