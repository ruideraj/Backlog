package com.ruideraj.backlog.lists

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.BacklogList
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

class ListsViewModel @Inject constructor(listsRepository: ListsRepository) : ViewModel() {

    val lists: LiveData<List<BacklogList>>
        get() = _lists
    private val _lists = MutableLiveData<List<BacklogList>>()

    init {
        viewModelScope.launch { listsRepository.loadLists().collect { lists -> _lists.value = lists } }
    }

}