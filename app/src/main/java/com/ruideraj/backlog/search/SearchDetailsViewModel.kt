package com.ruideraj.backlog.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.R
import com.ruideraj.backlog.SearchResult
import com.ruideraj.backlog.data.ApiException
import com.ruideraj.backlog.data.SearchRepository
import com.ruideraj.backlog.util.Strings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException
import java.text.DateFormat
import javax.inject.Inject

@HiltViewModel
class SearchDetailsViewModel @Inject constructor(private val searchRepository: SearchRepository,
                                                 private val strings: Strings) : ViewModel() {

    sealed class State {
        object Loading : State()
//        data class Loaded(val result: SearchResult) : State()
        data class Loaded(val label1: String,
                          val label2: String,
                          val label3: String,
                          val field1: String?,
                          val field2: String?,
                          val field3: String?) : State()
        object Error : State()
    }

    sealed class Event {
        data class ConfirmDetails(val searchResult: SearchResult) : Event()
    }

    private val _detailsState = MutableLiveData<State>()
    val detailsState: LiveData<State> = _detailsState

    private val eventChannel = Channel<Event>(Channel.BUFFERED)
    val eventFlow = eventChannel.receiveAsFlow()

    private var dataLoaded = false
    private lateinit var result: SearchResult

    fun loadDetails(inputResult: SearchResult) {
        if (!dataLoaded) {
            viewModelScope.launch {
                val state = try {
                    val searchResult = searchRepository.getDetails(inputResult)
                    result = searchResult
                    dataLoaded = true
                    if (searchResult.type == MediaType.FILM) {
                        val filmData = searchResult.metadata as Metadata.FilmData

                        val releaseDateText = if (filmData.releaseDate != null) {
                            DateFormat.getDateInstance().format((filmData.releaseDate))
                        } else null

                        State.Loaded(
                            strings.getString(R.string.field_creator_director),
                            strings.getString(R.string.field_creator_actors),
                            strings.getString(R.string.field_date_release),
                            filmData.director,
                            filmData.actors,
                            releaseDateText)
                    } else throw IllegalStateException("Only films are currently supported for search details")
                } catch (e: ApiException) {
                    State.Error
                } catch (e: IOException) {
                    State.Error
                } catch (e: HttpException) {
                    State.Error
                }

                _detailsState.value = state
            }
        }
    }

    fun onConfirm() {
        val state = _detailsState.value
        if (state != null && state is State.Loaded) {
            viewModelScope.launch {
                eventChannel.send(Event.ConfirmDetails(result))
            }
        }
    }
}