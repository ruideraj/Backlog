package com.ruideraj.backlog.search

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.ruideraj.backlog.*
import com.ruideraj.backlog.util.EntryField
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.DateFormat

@AndroidEntryPoint
class SearchDetailsDialog : DialogFragment() {

    companion object {
        private const val TAG = "SearchDetailsDialog"
    }

    private val viewModel by viewModels<SearchDetailsViewModel>()
    private lateinit var createdView: View

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createdView = requireActivity().layoutInflater.inflate(R.layout.dialog_search_details, null)

        return AlertDialog.Builder(requireContext()).apply {
            setView(createdView)
            setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.onConfirm()
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, dialog should be automatically dismissed
            }
        }.create().apply {
            setCanceledOnTouchOutside(false)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = createdView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()
        val searchResult = args.getParcelable<SearchResult>(Constants.ARG_SEARCH_RESULT)
            ?: throw IllegalStateException("Must include SearchResult to fetch details")

        val field1 = view.findViewById<EntryField>(R.id.search_details_field1)
        val field2 = view.findViewById<EntryField>(R.id.search_details_field2)
        val field3 = view.findViewById<EntryField>(R.id.search_details_field3)
        val progress = view.findViewById<ProgressBar>(R.id.search_details_progress)
        val message = view.findViewById<TextView>(R.id.search_details_message)

        view.findViewById<TextView>(R.id.search_details_title).text = searchResult.title

        val image = view.findViewById<ImageView>(R.id.search_details_image)
        Glide.with(image.context)
            .load(searchResult.metadata.imageUrl)
            .circleCrop()
            .placeholder(getImageForType(searchResult.type))
            .into(image)

        viewModel.let {
            it.loadDetails(searchResult)

            it.detailsState.observe(viewLifecycleOwner) { state ->
                field1.isInvisible = state !is SearchDetailsViewModel.State.Loaded
                field2.isInvisible = state !is SearchDetailsViewModel.State.Loaded
                field3.isInvisible = state !is SearchDetailsViewModel.State.Loaded
                progress.isInvisible = state !is SearchDetailsViewModel.State.Loading
                message.isInvisible = state !is SearchDetailsViewModel.State.Error

                when (state) {
                    is SearchDetailsViewModel.State.Loaded -> {
                        field1.hint = state.label1
                        field2.hint = state.label2
                        field3.hint = state.label3

                        field1.text = state.field1
                        field2.text = state.field2
                        field3.text = state.field3
                    }
                    SearchDetailsViewModel.State.Error -> {
                        (dialog as? AlertDialog)?.getButton(AlertDialog.BUTTON_POSITIVE)?.isEnabled = false
                    }
                    else -> { }
                }
            }

            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.STARTED) {
                    it.eventFlow.collect { event ->
                        when (event) {
                            is SearchDetailsViewModel.Event.ConfirmDetails -> {
                                findNavController().run {
                                    previousBackStackEntry?.savedStateHandle
                                        ?.set(Constants.ARG_SEARCH_RESULT, event.searchResult)
                                    navigateUp()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getImageForType(type: MediaType): Int {
        return when (type) {
            MediaType.FILM -> R.drawable.ic_film_40
            MediaType.SHOW -> R.drawable.ic_show_40
            MediaType.GAME -> R.drawable.ic_game_40
            MediaType.BOOK -> R.drawable.ic_book_40
        }
    }

}