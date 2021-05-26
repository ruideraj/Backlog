package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import dagger.hilt.android.lifecycle.HiltViewModel
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EntryEditViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val TAG = "EntryEditViewModel"
        private const val NOT_SHOWN = -1
    }

    enum class ShownFields(val releaseDate: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, R.string.field_creator_director, NOT_SHOWN),
        TV(R.string.field_date_first_aired, NOT_SHOWN, NOT_SHOWN),
        GAME(R.string.field_date_release, R.string.field_creator_developer, NOT_SHOWN),
        BOOK(R.string.field_date_publication, R.string.field_creator_author, R.string.field_creator_publisher)
    }

    private val _fields = MutableLiveData<ShownFields>()
    val fields: LiveData<ShownFields> = _fields

    private var date: Calendar? = null
    private val _releaseDate = MutableLiveData<String>()
    val releaseDate: LiveData<String> = _releaseDate

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    fun setType(type: MediaType) {
        _fields.value = when (type) {
            MediaType.FILM -> ShownFields.FILM
            MediaType.TV -> ShownFields.TV
            MediaType.GAME -> ShownFields.GAME
            MediaType.BOOK -> ShownFields.BOOK
        }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)

            val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
            _releaseDate.value = dateFormat.format(time)
        }
    }

    fun onClickConfirm(title: String, imageUrl: String, creator1: String, creator2: String) {
        if (title.isBlank()) {
            _titleError.value = true
        } else {
            // TODO Create new Entry for the given List
        }
    }

    fun onTitleTextChanged(input: String) {
        if (_titleError.value == true && input.isNotBlank()) _titleError.value = false
    }
}