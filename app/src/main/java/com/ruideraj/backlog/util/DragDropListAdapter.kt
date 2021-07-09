package com.ruideraj.backlog.util

import androidx.recyclerview.widget.*
import com.ruideraj.backlog.lists.DragDropHelperCallback

abstract class DragDropListAdapter<T, VH : RecyclerView.ViewHolder> : ListAdapter<T, VH> {
    constructor(diffCallback: DiffUtil.ItemCallback<T>) : super(diffCallback)
    constructor(config: AsyncDifferConfig<T>) : super(config)

    private lateinit var backingList: MutableList<T>

    override fun submitList(list: List<T>?) {
        // Using a mutable list allows us to use drag and drop without performing database I/O
        // while the user is moving the item around.
        backingList = mutableListOf<T>().apply {
            list?.let { addAll(list) }
        }
        super.submitList(backingList)
    }

    /**
     * Moves the item at index `fromPos` to `toPos`.
     *
     * This is needed in order to take advantage of [notifyItemMoved] which animates the items in the UI while the
     * user is dragging an item during drag-and-drop.  Without this, an extra animation plays after the drag-and-drop
     * is completed due to the new list differing from the unmodified backing list within [ListAdapter]
     */
    fun moveItem(fromPos: Int, toPos: Int) {
        /*
        Simple swap to move the item because the alternative, shifting all affected items to make room for the move,
        would be inefficient.
        Since this function is used for drag-and-drop the user will have to move the item over the intermediate
        positions, thus swapping the items into their correct positions along the way.
        Any potential inconsistencies should be corrected when the updated list from data sources
        (which should be identical to the now modified backing list) is submitted in submitList().
         */
        val temp = backingList[fromPos]
        backingList[fromPos] = backingList[toPos]
        backingList[toPos] = temp

        notifyItemMoved(fromPos, toPos)
    }

}