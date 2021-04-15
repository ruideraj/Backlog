package com.ruideraj.backlog.lists

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * Scrolls the given [RecyclerView] to the bottom when an item is added.
 */
class ScrollOnAddObserver(private val recycler: RecyclerView) : RecyclerView.AdapterDataObserver() {

    override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
        super.onItemRangeInserted(positionStart, itemCount)

        val count = recycler.adapter!!.itemCount
        val lastVisiblePosition =
            (recycler.layoutManager as LinearLayoutManager).findLastCompletelyVisibleItemPosition()

        if (lastVisiblePosition == -1 || positionStart >= count - 1 &&
            lastVisiblePosition == positionStart - 1) {
            recycler.scrollToPosition(positionStart)
        } else {
            recycler.scrollToPosition(count - 1);
        }
    }

}