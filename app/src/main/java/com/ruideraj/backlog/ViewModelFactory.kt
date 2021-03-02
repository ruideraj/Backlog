package com.ruideraj.backlog

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.ruideraj.backlog.lists.ListsViewModel

class ViewModelFactory(context: Context) : ViewModelProvider.Factory {

    private val appComponent = (context.applicationContext as BacklogApp).appComponent

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ListsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return appComponent.listsViewModel() as T
        }

        throw IllegalArgumentException("Unknown ViewModel Class")
    }
}