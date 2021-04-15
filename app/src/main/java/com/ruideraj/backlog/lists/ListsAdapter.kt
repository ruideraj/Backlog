package com.ruideraj.backlog.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.R

class ListsAdapter(val viewModel: ListsViewModel) : ListAdapter<BacklogList,
        RecyclerView.ViewHolder>(ListItemCallback()) {

    private lateinit var backingList: MutableList<BacklogList>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_list, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val list = getItem(position)
        val resources = vh.title.resources

        vh.title.text = list.title
        vh.detail.text = resources.getQuantityString(R.plurals.items, list.count, list.count)
        vh.icon.setImageResource(getIconResource(list.icon))
    }

    private fun getIconResource(icon: ListIcon) = when(icon) {
        ListIcon.LIST -> R.drawable.ic_list
        ListIcon.FILM -> R.drawable.ic_film
        ListIcon.TV -> R.drawable.ic_tv
        ListIcon.GAME -> R.drawable.ic_game
        ListIcon.BOOK -> R.drawable.ic_book
    }

    override fun submitList(list: List<BacklogList>?) {
        // Using a mutable list allows us to use drag and drop without performing database I/O
        // while the user is moving the item around.
        backingList = mutableListOf<BacklogList>().apply {
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
        Any potential inconsistencies should be corrected when the updated list (which should normally be identical
        to the now modified backing list) is submitted in submitList().
         */
        val temp = backingList[fromPos]
        backingList[fromPos] = backingList[toPos]
        backingList[toPos] = temp
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.list_title)
        val detail: TextView = itemView.findViewById(R.id.list_detail)
        val icon: ImageView = itemView.findViewById(R.id.list_icon)
        val overflow: ImageView = itemView.findViewById(R.id.list_overflow)

        init {
            overflow.setOnClickListener { view ->
                PopupMenu(view.context, view).apply {
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.list_action_edit -> {
                                viewModel.onClickEditList(adapterPosition)
                                true
                            }
                            R.id.list_action_delete -> {
                                viewModel.onClickDeleteList(adapterPosition)
                                true
                            }
                            else -> false
                        }
                    }
                    inflate(R.menu.menu_list_item)
                    show()
                }
            }
        }
    }

    private class ListItemCallback : DiffUtil.ItemCallback<BacklogList>() {
        override fun areItemsTheSame(oldItem: BacklogList, newItem: BacklogList) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: BacklogList, newItem: BacklogList) = oldItem == newItem
    }
}