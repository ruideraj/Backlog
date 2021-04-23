package com.ruideraj.backlog.lists

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.R
import com.ruideraj.backlog.util.collectWhileStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListsFragment : Fragment() {

    companion object {
        private const val TAG = "ListsFragment"
        private const val LIST_DIALOG_TAG = "ListDialog"
        private const val DELETE_DIALOG_TAG = "DeleteDialog"
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

        recycler = view.findViewById<RecyclerView>(R.id.lists_recycler).apply {
            layoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(this.context, DividerItemDecoration.VERTICAL))
        }

        adapter = ListsAdapter(viewModel).apply {
            registerAdapterDataObserver(ScrollOnAddObserver(recycler))
        }
        recycler.adapter = adapter

        val dragDropCallback = DragDropHelperCallback(adapter, { dragStartPosition ->
            viewModel.moveListStarted(dragStartPosition)
        }, { dragEndPosition ->
            viewModel.moveListEnded(dragEndPosition)
        })
        ItemTouchHelper(dragDropCallback).attachToRecyclerView(recycler)

        val fab = view.findViewById<FloatingActionButton>(R.id.lists_button_create).apply {
            setOnClickListener { viewModel.onClickCreateList() }
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

        viewModel.let { it ->
            it.lists.observe(requireActivity(), { lists ->
                adapter.submitList(lists)
            })

            it.eventFlow.collectWhileStarted(this) { event ->
                when (event) {
                    is ListsViewModel.Event.ShowCreateList -> {
                        val args = Bundle().apply {
                            putInt(Constants.ARG_MODE, Constants.MODE_CREATE)
                            putSerializable(Constants.ARG_ICON, event.defaultIcon)
                        }
                        showListDialog(args)
                    }
                    is ListsViewModel.Event.ShowEditList -> {
                        val args = Bundle().apply {
                            putInt(Constants.ARG_MODE, Constants.MODE_EDIT)
                            putLong(Constants.ARG_LIST_ID, event.listId)
                            putString(Constants.ARG_TITLE, event.title)
                            putSerializable(Constants.ARG_ICON, event.icon)
                        }
                        showListDialog(args)
                    }
                    is ListsViewModel.Event.ShowDeleteDialog -> {
                        val args = Bundle().apply { putParcelable(Constants.ARG_LIST, event.list) }
                        DeleteListDialogFragment().let { dialog ->
                            dialog.arguments = args
                            dialog.show(childFragmentManager, DELETE_DIALOG_TAG)
                        }
                    }
                    is ListsViewModel.Event.CloseListDialog -> {
                        childFragmentManager.findFragmentByTag(LIST_DIALOG_TAG)?.let {
                            if ((it as DialogFragment).isVisible) it.dismiss()
                        }
                    }
                }
            }
        }
    }

    private fun showListDialog(args: Bundle) {
        ListDialogFragment().apply {
            arguments =args
            show(this@ListsFragment.childFragmentManager, LIST_DIALOG_TAG)
        }
    }

}