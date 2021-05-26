package com.ruideraj.backlog.entries

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.Status

class EntriesAdapter(private val viewModel: EntriesViewModel)
    : ListAdapter<Entry, RecyclerView.ViewHolder>(EntryCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_entry, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val entry = getItem(position)

        vh.title.text = entry.title
        vh.image.setImageResource(getImageForType(entry.type))

        if (entry.status == Status.TODO) {
            vh.status.setImageDrawable(null)
        } else {
            vh.status.setImageResource(getImageForStatus(entry.status))
        }
    }

    private fun getImageForType(type: MediaType): Int {
        return when (type) {
            MediaType.FILM -> R.drawable.ic_film
            MediaType.TV -> R.drawable.ic_tv
            MediaType.GAME -> R.drawable.ic_game
            MediaType.BOOK -> R.drawable.ic_book
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
        val title: TextView = itemView.findViewById(R.id.entry_title)
        val image: ImageView = itemView.findViewById(R.id.entry_image)
        val status: ImageView = itemView.findViewById(R.id.entry_status)

        init {
            itemView.setOnClickListener {
                viewModel.onClickEntry(adapterPosition)
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