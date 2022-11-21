package com.ruideraj.backlog.entries

import androidx.lifecycle.*
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
    private val strings: Strings,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "EntryEditViewModel"
        private const val NOT_SHOWN = -1

        private const val ARG_EDIT_MODE = "editMode"
        private const val ARG_TITLE = "title"
        private const val ARG_RELEASE_DATE = "date"
        private const val ARG_YEAR = "year"
        private const val ARG_IMAGE_URL = "imageUrl"
        private const val ARG_CREATOR1 = "creator1"
        private const val ARG_CREATOR2 = "creator2"
    }

    sealed class Event {
        object GoBackToList : Event()
        data class PopulateFields(
            val title: String?,
            val releaseYear: String?,
            val imageUrl: String?,
            val creator1: String?,
            val creator2: String?
        ) : Event()
        object GoToSearch : Event()
    }

    enum class ShownFields(val releaseDate: Int, val releaseYear: Int, val creator1: Int, val creator2: Int) {
        FILM(R.string.field_date_release, NOT_SHOWN, R.string.field_creator_director, R.string.field_creator_actors),
        SHOW(NOT_SHOWN, R.string.field_run_dates, NOT_SHOWN, NOT_SHOWN),
        GAME(R.string.field_date_release, NOT_SHOWN, R.string.field_creator_developer, NOT_SHOWN),
        BOOK(NOT_SHOWN, R.string.field_year_first_year_published, R.string.field_creator_author, NOT_SHOWN)
    }

    private var listId: Long = -1L
    private lateinit var mediaType: MediaType
    private var existingEntry: Entry? = null

    private val _screenTitle = MutableLiveData<String>()
    val screenTitle: LiveData<String> = _screenTitle

    private val _editMode = MutableLiveData(true)
    val editMode: LiveData<Boolean> = _editMode

    private val _showCloseIcon = MutableLiveData(false)
    val showCloseIcon: LiveData<Boolean> = _showCloseIcon

    private val _menuState = MutableLiveData(EntryEditMenuState())
    val menuState: LiveData<EntryEditMenuState> = _menuState

    private val _fields = MutableLiveData<ShownFields>()
    val fields: LiveData<ShownFields> = _fields

    var date: Calendar? = null
        private set
    private val _releaseDateText = MutableLiveData<String>()
    val releaseDate: LiveData<String> = _releaseDateText

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

    fun initialize(listId: Long, type: MediaType, entry: Entry? = null, recreating: Boolean) {
        this.listId = listId
        if (listId < 0) throw IllegalArgumentException("Must include valid list id")

        mediaType = type
        existingEntry = entry

        _fields.value = when (type) {
            MediaType.FILM -> ShownFields.FILM
            MediaType.SHOW -> ShownFields.SHOW
            MediaType.GAME -> ShownFields.GAME
            MediaType.BOOK -> ShownFields.BOOK
        }

        _screenTitle.value = if (entry != null) {
            strings.getString(R.string.entry_view)
        } else {
            val typeRes = when (type) {
                MediaType.FILM -> R.string.film
                MediaType.SHOW -> R.string.show
                MediaType.GAME -> R.string.game
                MediaType.BOOK -> R.string.book
            }
            strings.getString(R.string.entry_title, strings.getString(typeRes))
        }

        if (recreating) {
            savedStateHandle.run {
                val editMode = get<Boolean>(ARG_EDIT_MODE)
                val releaseDate = get<Calendar>(ARG_RELEASE_DATE)

                setEditMode(editMode == true)
                date = releaseDate
                _releaseDateText.value = releaseDate?.let { convertDateToString(it.time) }
            }
        } else if (entry != null) {
            setFieldData(entry.title, entry.metadata)
            setEditMode(false)
        } else {
            setEditMode(true)
        }
    }

    fun saveState() {
        savedStateHandle.run {
            set(ARG_EDIT_MODE, _editMode.value)
            set(ARG_RELEASE_DATE, date)
        }
    }

    fun onClickEditMode() {
        setEditMode(true)
    }

    fun onClickNavigationIcon() {
        if (existingEntry != null && _editMode.value == true) {
            setEditMode(false)
            existingEntry?.let { setFieldData(it.title, it.metadata) }
        } else {
            viewModelScope.launch { eventChannel.send(Event.GoBackToList) }
        }
    }

    fun onBackPressed() {
        onClickNavigationIcon()
    }

    fun onClickSearch() {
        viewModelScope.launch { eventChannel.send(Event.GoToSearch) }
    }

    fun onDateSelected(year: Int, month: Int, day: Int) {
        date = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)

            _releaseDateText.value = convertDateToString(time)
        }
    }

    fun submit(title: String?, year: String?, imageUrl: String?, creator1: String?, creator2: String?) {
        if (title.isNullOrBlank()) {
            _titleError.value = true
            return
        }

        val metadata = when (mediaType) {
            MediaType.FILM -> Metadata.FilmData(creator1, creator2, date?.time, imageUrl, null)
            MediaType.SHOW -> Metadata.ShowData(year, imageUrl, null)
            MediaType.GAME -> Metadata.GameData(creator1, date?.time, imageUrl)
            MediaType.BOOK -> {
                val yearVal = if (!year.isNullOrBlank()) {
                    try {
                        Year.of(year.toInt())
                    } catch (e: NumberFormatException) {
                        _yearError.value = true;
                        return
                    }
                } else {
                    null
                }

                Metadata.BookData(creator1, yearVal, imageUrl)
            }
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
        val state = EntryEditMenuState(existingEntry != null && !enable, enable, enable)
        _menuState.value = state

        if (existingEntry != null) {
            _showCloseIcon.value = enable
        }
    }

    private fun setFieldData(title: String, metadata: Metadata) {
        var creator1: String? = null
        var creator2: String? = null
        var releaseDate: String? = null
        var releaseYear: String? = null
        when (metadata) {
            is Metadata.FilmData -> {
                creator1 = metadata.director
                creator2 = metadata.actors
                if (metadata.releaseDate != null) {
                    releaseDate = convertDateToString(metadata.releaseDate)
                    date = Calendar.getInstance().apply { time = metadata.releaseDate }
                }
            }
            is Metadata.ShowData -> {
                releaseYear = metadata.runDates
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
        }

        viewModelScope.launch {
            eventChannel.send(
                Event.PopulateFields(
                    title,
                    releaseYear,
                    metadata.imageUrl,
                    creator1,
                    creator2
                )
            )
        }
        releaseDate?.let { _releaseDateText.value = it }
    }

    private fun convertDateToString(date: Date): String {
        val dateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault())
        return dateFormat.format(date)
    }
}