package com.ruideraj.backlog.lists

import android.util.Log
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

class ListsAdapter(viewModel: ListsViewModel) : ListAdapter<BacklogList, RecyclerView.ViewHolder>(ListItemCallback()) {

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
        ListIcon.SHOW -> R.drawable.ic_tv
        ListIcon.GAME -> R.drawable.ic_game
        ListIcon.BOOK -> R.drawable.ic_book
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
                                Log.d("ListAdapter", "edit: $adapterPosition")
                                true
                            }
                            R.id.list_action_delete -> {
                                Log.d("ListAdapter", "delete: $adapterPosition")
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
        override fun areItemsTheSame(oldItem: BacklogList, newItem: BacklogList) = oldItem.listId == newItem.listId

        override fun areContentsTheSame(oldItem: BacklogList, newItem: BacklogList) = oldItem == newItem
    }
}