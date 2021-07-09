package com.ruideraj.backlog.lists

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.util.DragDropListAdapter

class DragDropHelperCallback<T, VH: RecyclerView.ViewHolder>(private val adapter: DragDropListAdapter<T, VH>,
                             private val onDragStarted: (dragStartPosition: Int) -> Unit,
                             private val onDragFinished: (dragEndPosition: Int) -> Unit )
    : ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0) {

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        super.onSelectedChanged(viewHolder, actionState)

        if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
            viewHolder?.let { onDragStarted.invoke(it.adapterPosition) }
        }
    }

    override fun onMove(recyclerView: RecyclerView,
                        viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        // Not used, do nothing
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        super.clearView(recyclerView, viewHolder)
        onDragFinished.invoke(viewHolder.adapterPosition)
    }

    override fun isItemViewSwipeEnabled() = false
}