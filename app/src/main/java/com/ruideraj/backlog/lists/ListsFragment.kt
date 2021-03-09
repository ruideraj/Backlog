package com.ruideraj.backlog.lists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListsFragment : Fragment() {

    companion object {
        private const val TAG = "ListsFragment"
        private const val LIST_DIALOG_TAG = "ListDialog"
    }

    private val viewModel by viewModels<ListsViewModel>()
    private lateinit var recycler: RecyclerView
    private lateinit var adapter: ListsAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)

        recycler = view.findViewById(R.id.lists_recycler)
        recycler.layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
        recycler.addItemDecoration(DividerItemDecoration(recycler.context, DividerItemDecoration.VERTICAL))
        adapter = ListsAdapter(viewModel).apply {
            // Scroll to the bottom when an item is added
            registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
                override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                    super.onItemRangeInserted(positionStart, itemCount)

                    val count = adapter.itemCount
                    val lastVisiblePosition =
                        (recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

                    if (lastVisiblePosition == -1 || positionStart >= count - 1 &&
                        lastVisiblePosition == positionStart - 1) {
                        recycler.scrollToPosition(positionStart)
                    } else {
                        recycler.scrollToPosition(count - 1);
                    }
                }
            })
        }
        recycler.adapter = adapter

        val fab = view.findViewById<FloatingActionButton>(R.id.lists_button_create).apply {
            setOnClickListener {
                val dialogFragment = ListDialogFragment()
                dialogFragment.show(childFragmentManager, LIST_DIALOG_TAG)
            }
        }

        recycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy > 0) {
                    fab.hide()
                } else if (dy < 0) {
                    fab.show()
                }
            }
        })

        viewModel.let {
            it.lists.observe(requireActivity(), { lists ->
                Log.d(TAG, "Submitting new list to adapter")
                adapter.submitList(lists)
            })
        }
    }

}