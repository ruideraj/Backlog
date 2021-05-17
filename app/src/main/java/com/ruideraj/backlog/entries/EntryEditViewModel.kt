package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EntryEditViewModel @Inject constructor() : ViewModel() {

    enum class ShownFields(val releaseDate: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, R.string.field_creator_director, -1),
        TV(R.string.field_date_first_aired, -1, -1),
        GAME(R.string.field_date_release, R.string.field_creator_developer, -1),
        BOOK(R.string.field_date_publication, R.string.field_creator_author, R.string.field_creator_publisher)
    }

    private val _fields = MutableLiveData<ShownFields>()
    val fields: LiveData<ShownFields> = _fields

    fun setType(type: MediaType) {
        _fields.value = when (type) {
            MediaType.FILM -> ShownFields.FILM
            MediaType.TV -> ShownFields.TV
            MediaType.GAME -> ShownFields.GAME
            MediaType.BOOK -> ShownFields.BOOK
        }
    }
}