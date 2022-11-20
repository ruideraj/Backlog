package com.ruideraj.backlog.entries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.Status
import com.ruideraj.backlog.util.DragDropListAdapter

class EntriesAdapter(private val viewModel: EntriesViewModel)
    : DragDropListAdapter<Entry, RecyclerView.ViewHolder>(EntryCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_entry, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val entry = getItem(position)

        vh.title.text = entry.title

        Glide.with(vh.image.context)
            .load(entry.metadata.imageUrl)
            .circleCrop()
            .placeholder(getImageForType(entry.type))
            .into(vh.image)

        if (entry.status == Status.TODO) {
            vh.status.setImageDrawable(null)
        } else {
            vh.status.setImageResource(getImageForStatus(entry.status))
        }

        if (viewModel.selectedEntries.contains(entry)) {
            vh.content.setBackgroundResource(R.color.bg_entry_selected)
        } else {
            vh.content.setBackgroundResource(0)
        }
    }

    private fun getImageForType(type: MediaType): Int {
        return when (type) {
            MediaType.FILM -> R.drawable.ic_film_40
            MediaType.SHOW -> R.drawable.ic_show_40
            MediaType.GAME -> R.drawable.ic_game_40
            MediaType.BOOK -> R.drawable.ic_book_40
        }
    }

    private fun getImageForStatus(status: Status): Int {
        return when (status) {
            Status.IN_PROGRESS -> R.drawable.ic_status_play
            Status.DONE -> R.drawable.ic_status_done
            else -> -1
        }
    }

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val content: ViewGroup = itemView.findViewById(R.id.entry_content)
        val title: TextView = itemView.findViewById(R.id.entry_title)
        val image: ImageView = itemView.findViewById(R.id.entry_image)
        val status: ImageView = itemView.findViewById(R.id.entry_status)

        init {
            itemView.let {
                it.setOnClickListener {
                    viewModel.onClickEntry(adapterPosition)
                }
            }

            status.setOnClickListener {
                viewModel.onClickEntryStatus(adapterPosition)
            }
        }
    }

    private class EntryCallback : DiffUtil.ItemCallback<Entry>() {
        override fun areItemsTheSame(oldItem: Entry, newItem: Entry) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Entry, newItem: Entry) = oldItem == newItem
    }
}