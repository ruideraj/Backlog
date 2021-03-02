package com.ruideraj.backlog.lists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.R
import com.ruideraj.backlog.ViewModelFactory

class ListsFragment : Fragment() {

    companion object {
        private const val TAG = "ListsFragment"
    }

    private val viewModel by viewModels<ListsViewModel> { ViewModelFactory(requireActivity()) }
    private lateinit var adapter: ListsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)

        val recycler = view.findViewById<RecyclerView>(R.id.lists_recycler)
        recycler.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
        adapter = ListsAdapter()
        recycler.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.lists_button_create).setOnClickListener {
            // TODO Open Create List dialog
        }

        viewModel.let {
            it.lists.observe(requireActivity(), { lists ->
                adapter.submitList(lists)
            })
        }
    }

}