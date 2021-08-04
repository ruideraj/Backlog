package com.ruideraj.backlog.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R

class SearchFragment : Fragment() {

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
        }
    }
}