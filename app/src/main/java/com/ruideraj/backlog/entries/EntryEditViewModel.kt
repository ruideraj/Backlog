package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlin.reflect.KProperty

@HiltViewModel
class EntryEditViewModel @Inject constructor() : ViewModel() {

    private val _fields = MutableLiveData<List<String>>()
    val fields: LiveData<List<String>> = _fields

    fun setType(type: MediaType) {
        _fields.value = getFieldsForType(type)
    }

    private fun getFieldsForType(type: MediaType) : List<String> {
        val metadataClass = when (type) {
            MediaType.FILM -> Metadata.FilmData::class
            MediaType.TV -> Metadata.ShowData::class
            MediaType.GAME -> Metadata.GameData::class
            MediaType.BOOK -> Metadata.BookData::class
        }

        return metadataClass.members.filterIsInstance<KProperty<Any>>().map { kProperty -> kProperty.name }
    }
}