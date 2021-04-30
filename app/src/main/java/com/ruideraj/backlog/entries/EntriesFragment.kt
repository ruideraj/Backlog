package com.ruideraj.backlog.entries

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntriesFragment : Fragment() {

    companion object {
        const val TAG = "EntriesFragment"
    }

    private val viewModel by viewModels<EntriesViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = requireArguments().getParcelable<BacklogList>(Constants.ARG_LIST)
            ?: throw IllegalStateException("Need to provide a list to Entries screen")

        (requireActivity() as AppCompatActivity).supportActionBar?.title = list.title

        val recycler = view.findViewById<RecyclerView>(R.id.entries_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        val adapter = EntriesAdapter(viewModel)
        recycler.adapter = adapter

        viewModel.let {
            it.entries.observe(viewLifecycleOwner) { entriesList ->
                adapter.submitList(entriesList)
            }
        }

        viewModel.loadEntries(list.id)
    }

}