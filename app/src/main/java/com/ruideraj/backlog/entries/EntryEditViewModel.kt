package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.R
import com.ruideraj.backlog.data.EntriesRepository
import com.ruideraj.backlog.util.Strings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class EntryEditViewModel @Inject constructor(
    private val entriesRepository: EntriesRepository,
    private val strings: Strings
) : ViewModel() {

    companion object {
        private const val TAG = "EntryEditViewModel"
        private const val NOT_SHOWN = -1
    }

    sealed class Event {
        object GoBackToList : Event()
        data class PopulateFields(
            val title: String,
            val imageUrl: String?,
            val creator1: String?,
            val creator2: String?
        ) : Event()
        object GoToSearch : Event()
    }

    enum class ShownFields(val releaseDate: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, R.string.field_creator_director, NOT_SHOWN),
        SHOW(R.string.field_date_first_aired, NOT_SHOWN, NOT_SHOWN),
        GAME(R.string.field_date_release, R.string.field_creator_developer, NOT_SHOWN),
        BOOK(R.string.field_date_publication, R.string.field_creator_author, NOT_SHOWN)
    }

    private var listId: Long = -1L
    private lateinit var mediaType: MediaType
    private var existingEntry: Entry? = null

    private val _title = MutableLiveData<String>()
    val title: LiveData<String> = _title

    private val _editMode = MutableLiveData(true)
    val editMode: LiveData<Boolean> = _editMode

    private val _showCloseIcon = MutableLiveData(false)
    val showCloseIcon: LiveData<Boolean> = _showCloseIcon

    private val _showEditModeAction = MutableLiveData(false)
    val showEditModeAction: LiveData<Boolean> = _showEditModeAction

    private val _fields = MutableLiveData<ShownFields>()
    val fields: LiveData<ShownFields> = _fields

    private var date: Calendar? = null
    private val _releaseDate = MutableLiveData<String>()
    val releaseDate: LiveData<String> = _releaseDate

    private val _titleError = MutableLiveData(false)
    val titleError: LiveData<Boolean> = _titleError

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private var imageInputJob: Job? = null

    private val _imageError = MutableLiveData(false)
    val imageError: LiveData<Boolean> = _imageError

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun initialize(listId: Long, type: MediaType, entry: Entry? = null) {
        this.listId = listId
        if (listId < 0) throw IllegalArgumentException("Must include valid list id")

        mediaType = type

        _fields.value = when (type) {
            MediaType.FILM -> ShownFields.FILM
            MediaType.SHOW -> ShownFields.SHOW
            MediaType.GAME -> ShownFields.GAME
            MediaType.BOOK -> ShownFields.BOOK
        }

        if (entry != null) {
            _title.value = strings.getString(R.string.entry_view)
            existingEntry = entry
            setFieldData(entry)
            setEditMode(false)
        } else {
            val typeRes = when (type) {
                MediaType.FILM -> R.string.film
                MediaType.SHOW -> R.string.show
                MediaType.GAME -> R.string.game
                MediaType.BOOK -> R.string.book
            }
            _title.value = strings.getString(R.string.entry_title, strings.getString(typeRes))
        }
    }

    fun onClickEditMode() {
        setEditMode(true)
    }

    fun onClickNavigationIcon() {
        if (existingEntry != null && _editMode.value == true) {
            setEditMode(false)
        } else {
            viewModelScope.launch { eventChannel.send(Event.GoBackToList) }
        }
    }

    fun onClickSearch() {
        viewModelScope.launch { eventChannel.send(Event.GoToSearch) }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)

            _releaseDate.value = convertDateToString(time)
        }
    }

    fun submit(title: String, imageUrl: String?, creator1: String?, creator2: String?) {
        if (title.isBlank()) {
            _titleError.value = true
        } else {
            val metadata = when (mediaType) {
                MediaType.FILM -> Metadata.FilmData(creator1, date?.time, imageUrl)
                MediaType.SHOW -> Metadata.ShowData(date?.time, imageUrl)
                MediaType.GAME -> Metadata.GameData(creator1, date?.time, imageUrl)
                MediaType.BOOK -> Metadata.BookData(creator1, date?.time, imageUrl)
            }

            viewModelScope.launch {
                val entry = existingEntry
                if (entry != null) {
                    entriesRepository.editEntry(entry.id, title, metadata)
                } else {
                    entriesRepository.createEntry(listId, mediaType, title, metadata)
                }
                eventChannel.send(Event.GoBackToList)
            }
        }
    }

    fun onTitleTextChanged(input: String) {
        if (_titleError.value == true && input.isNotBlank()) _titleError.value = false
    }

    fun onImageTextChanged(input: String) {
        imageInputJob?.run { if (!isCompleted) cancel() }

        imageInputJob = viewModelScope.launch {
            delay(2000)

            if (_imageError.value == true) {
                _imageError.value = false
            }

            _imageUrl.value = input
        }
    }

    fun onImageLoadError() {
        _imageError.value = true
    }

    private fun setEditMode(enable: Boolean) {
        _editMode.value = enable
        if (existingEntry != null) {
            _showCloseIcon.value = enable
            _showEditModeAction.value = !enable
        }
    }

    private fun setFieldData(entry: Entry) {
        val title = entry.title
        val imageUrl = entry.metadata.imageUrl

        var creator1: String? = null
        var creator2: String? = null
        when (entry.metadata) {
            is Metadata.FilmData -> {
                creator1 = entry.metadata.director
            }
            is Metadata.GameData -> {
                creator1 = entry.metadata.developer
            }
            is Metadata.BookData -> {
                creator1 = entry.metadata.author
                //creator2 = entry.metadata.publisher
            }
            else -> {
            }
        }

        viewModelScope.launch {
            eventChannel.send(
                Event.PopulateFields(
                    title,
                    imageUrl,
                    creator1,
                    creator2
                )
            )
            entry.metadata.releaseDate?.let { _releaseDate.value = convertDateToString(it) }
        }
    }

    private fun convertDateToString(date: Date): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(date)
    }
}