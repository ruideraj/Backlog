package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.R
import com.ruideraj.backlog.data.EntriesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EntryEditViewModel @Inject constructor(private val entriesRepository: EntriesRepository) : ViewModel() {

    companion object {
        private const val TAG = "EntryEditViewModel"
        private const val NOT_SHOWN = -1
    }

    sealed class Event {
        object GoBackToList : Event()
    }

    enum class ShownFields(val releaseDate: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, R.string.field_creator_director, NOT_SHOWN),
        SHOW(R.string.field_date_first_aired, NOT_SHOWN, NOT_SHOWN),
        GAME(R.string.field_date_release, R.string.field_creator_developer, NOT_SHOWN),
        BOOK(R.string.field_date_publication, R.string.field_creator_author, R.string.field_creator_publisher)
    }

    private var listId: Long = -1L
    private lateinit var mediaType: MediaType

    private val _fields = MutableLiveData<ShownFields>()
    val fields: LiveData<ShownFields> = _fields

    private var date: Calendar? = null
    private val _releaseDate = MutableLiveData<String>()
    val releaseDate: LiveData<String> = _releaseDate

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun initialize(listId: Long, type: MediaType) {
        this.listId = listId
        if (listId < 0) throw IllegalArgumentException("Must include valid list id")

        mediaType = type

        _fields.value = when (type) {
            MediaType.FILM -> ShownFields.FILM
            MediaType.SHOW -> ShownFields.SHOW
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

    fun createEntry(title: String, imageUrl: String?, creator1: String?, creator2: String?) {
        if (title.isBlank()) {
            _titleError.value = true
        } else {
            val metadata = when (mediaType) {
                MediaType.FILM -> Metadata.FilmData(creator1, date?.time, imageUrl)
                MediaType.SHOW -> Metadata.ShowData(date?.time, imageUrl)
                MediaType.GAME -> Metadata.GameData(creator1, date?.time, imageUrl)
                MediaType.BOOK -> Metadata.BookData(creator1, creator2, date?.time, imageUrl)
            }

            viewModelScope.launch {
                entriesRepository.createEntry(listId, mediaType, title, metadata)
                eventChannel.send(Event.GoBackToList)
            }
        }
    }

    fun onTitleTextChanged(input: String) {
        if (_titleError.value == true && input.isNotBlank()) _titleError.value = false
    }
}