package com.ruideraj.backlog.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchFragment : Fragment() {

    companion object {
        private const val TAG = "SearchFragment"
    }

    private val viewModel by viewModels<SearchViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        val type = args.getSerializable(Constants.ARG_TYPE) as MediaType

        val toolbar = view.findViewById<Toolbar>(R.id.search_toolbar).apply {
            setNavigationIcon(R.drawable.ic_arrow_back)
            setNavigationOnClickListener { findNavController().navigateUp() }
        }

        val searchInput = toolbar.findViewById<SearchView>(R.id.search_input).apply {
            setIconifiedByDefault(false)
            maxWidth = Integer.MAX_VALUE
            val typePluralRes = when (type) {
                MediaType.FILM -> R.string.films
                MediaType.SHOW -> R.string.shows
                MediaType.GAME -> R.string.games
                MediaType.BOOK -> R.string.books
            }
            queryHint = getString(R.string.search_hint, getString(typePluralRes))

            setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextChange(newText: String?): Boolean {
                    viewModel.onSearchInputChanged(type, newText)
                    return true
                }

                override fun onQueryTextSubmit(query: String?): Boolean {
                    viewModel.onSearchInputChanged(type, query)
                    return true
                }
            })
        }
    }
}