package com.ruideraj.backlog.entries

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.*
import com.ruideraj.backlog.data.EntriesRepository
import com.ruideraj.backlog.util.Strings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.DateFormat
import java.time.Year
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
            val releaseYear: String?,
            val imageUrl: String?,
            val creator1: String?,
            val creator2: String?
        ) : Event()
        object GoToSearch : Event()
    }

    enum class ShownFields(val releaseDate: Int, val releaseYear: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, NOT_SHOWN, R.string.field_creator_director, NOT_SHOWN),
        SHOW(R.string.field_date_first_aired, NOT_SHOWN, NOT_SHOWN, NOT_SHOWN),
        GAME(R.string.field_date_release, NOT_SHOWN, R.string.field_creator_developer, NOT_SHOWN),
        BOOK(NOT_SHOWN, R.string.field_year_first_year_published, R.string.field_creator_author, NOT_SHOWN)
    }

    private var initialized = false
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

    private val _yearError = MutableLiveData(false)
    val yearError: LiveData<Boolean> = _yearError

    private val _imageUrl = MutableLiveData<String>()
    val imageUrl: LiveData<String> = _imageUrl

    private var imageInputJob: Job? = null

    private val _imageError = MutableLiveData(false)
    val imageError: LiveData<Boolean> = _imageError

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    fun initialize(listId: Long, type: MediaType, entry: Entry? = null) {
        if (initialized) return

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
            setFieldData(entry.title, entry.metadata)
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

        initialized = true
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

    fun onDateSelected(year: Int, month: Int, day: Int) {
        date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)

            _releaseDate.value = convertDateToString(time)
        }
    }

    fun submit(title: String, year: String?, imageUrl: String?, creator1: String?, creator2: String?) {
        if (title.isBlank()) {
            _titleError.value = true
            return
        }

        val yearVal = if (mediaType == MediaType.BOOK && !year.isNullOrBlank()) {
            try {
                Year.of(year.toInt())
            } catch (e: NumberFormatException) {
                _yearError.value = true;
                return
            }
        } else {
            null
        }

        val metadata = when (mediaType) {
            MediaType.FILM -> Metadata.FilmData(creator1, date?.time, imageUrl)
            MediaType.SHOW -> Metadata.ShowData(date?.time, imageUrl)
            MediaType.GAME -> Metadata.GameData(creator1, date?.time, imageUrl)
            MediaType.BOOK -> Metadata.BookData(creator1, yearVal, imageUrl)
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

    fun onTitleTextChanged(input: String) {
        if (_titleError.value == true && input.isNotBlank()) _titleError.value = false
    }

    fun onYearTextChanged() {
        if (_yearError.value == true) _yearError.value = false
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

    fun onSearchResultReceived(searchResult: SearchResult) {
        setFieldData(searchResult.title, searchResult.metadata)
    }

    private fun setEditMode(enable: Boolean) {
        _editMode.value = enable
        if (existingEntry != null) {
            _showCloseIcon.value = enable
            _showEditModeAction.value = !enable
        }
    }

    private fun setFieldData(title: String, metadata: Metadata) {
        val imageUrl = metadata.imageUrl

        var creator1: String? = null
        var creator2: String? = null
        var releaseDate: String? = null
        var releaseYear: String? = null
        when (metadata) {
            is Metadata.FilmData -> {
                creator1 = metadata.director
                if (metadata.releaseDate != null) {
                    releaseDate = convertDateToString(metadata.releaseDate)
                    date = Calendar.getInstance().apply { time = metadata.releaseDate }
                }
            }
            is Metadata.GameData -> {
                creator1 = metadata.developer
                if (metadata.releaseDate != null) {
                    releaseDate = convertDateToString(metadata.releaseDate)
                    date = Calendar.getInstance().apply { time = metadata.releaseDate }
                }
            }
            is Metadata.BookData -> {
                creator1 = metadata.author
                releaseYear = metadata.yearPublished?.toString()
                //creator2 = entry.metadata.publisher
            }
            else -> {
            }
        }

        viewModelScope.launch {
            eventChannel.send(
                Event.PopulateFields(
                    title,
                    releaseYear,
                    imageUrl,
                    creator1,
                    creator2
                )
            )
        }
        releaseDate?.let { _releaseDate.value = it }
    }

    private fun convertDateToString(date: Date): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(date)
    }
}