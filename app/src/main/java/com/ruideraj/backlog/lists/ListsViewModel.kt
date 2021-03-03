package com.ruideraj.backlog.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListsViewModel @Inject constructor(private val listsRepository: ListsRepository) : ViewModel() {

    val lists: LiveData<List<BacklogList>>
        get() = _lists
    private val _lists = MutableLiveData<List<BacklogList>>()

    init {
        viewModelScope.launch { listsRepository.loadLists().collect { lists -> _lists.value = lists } }
    }

    fun createList(title: String, icon: ListIcon) {
        viewModelScope.launch {
            listsRepository.createList(title, icon)
        }
    }

}