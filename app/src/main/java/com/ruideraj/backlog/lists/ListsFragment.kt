package com.ruideraj.backlog.lists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

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

        val helperSimpleCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                viewHolder?.let { viewModel.moveListStarted(it.adapterPosition) }
            }

            override fun onMove(recyclerView: RecyclerView,
                                viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                val fromPos = viewHolder.adapterPosition
                val toPos = target.adapterPosition

                adapter.run {
                    moveItem(fromPos, toPos)
                    notifyItemMoved(fromPos, toPos)
                }

                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not used, do nothing
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewModel.moveListEnded(viewHolder.adapterPosition)
            }

            override fun isItemViewSwipeEnabled() = false
        }
        ItemTouchHelper(helperSimpleCallback).attachToRecyclerView(recycler)

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

        viewModel.let {
            it.lists.observe(requireActivity(), { lists ->
                adapter.submitList(lists)
            })

            lifecycleScope.launchWhenStarted { it.openListDialog.collect { args ->
                ListDialogFragment().let { dialog ->
                    dialog.arguments = args
                    dialog.show(childFragmentManager, LIST_DIALOG_TAG)
                }
            } }

            lifecycleScope.launchWhenStarted { it.openDeleteDialog.collect { args ->
                DeleteListDialogFragment().let { dialog ->
                    dialog.arguments = args
                    dialog.show(childFragmentManager, DELETE_DIALOG_TAG)
                }
            } }
        }
    }

}