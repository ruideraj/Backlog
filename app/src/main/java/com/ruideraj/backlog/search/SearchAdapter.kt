package com.ruideraj.backlog.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.R
import com.ruideraj.backlog.SearchResult

class SearchAdapter : ListAdapter<SearchResult, RecyclerView.ViewHolder>(SearchCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_search_result, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val searchResult = getItem(position)

        vh.title.text = searchResult.title

        Glide.with(vh.image.context)
            .load(searchResult.metadata.imageUrl)
            .circleCrop()
            .placeholder(getImageForType(searchResult.type))
            .into(vh.image)

        // This field currently unused by all media types
        vh.field3.visibility = View.GONE
        vh.field3Icon.visibility = View.GONE

        when (searchResult.type) {
            MediaType.FILM -> {

            }
            MediaType.SHOW -> { }
            MediaType.GAME -> { }
            MediaType.BOOK -> {
                val metadata = searchResult.metadata as Metadata.BookData

                vh.field1Icon.setImageResource(R.drawable.ic_person)
                vh.field1.text = (metadata.author)

                vh.field2Icon.setImageResource(R.drawable.ic_event)
                metadata.yearPublished?.let { year ->
                    vh.field2.run { text = context.getString(R.string.search_field_published, year.value) }
                }
            }
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

    private inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.search_item_title)
        val image: ImageView = itemView.findViewById(R.id.search_item_image)
        val field1: TextView = itemView.findViewById(R.id.search_item_field1)
        val field1Icon: ImageView = itemView.findViewById(R.id.search_item_field1_icon)
        val field2: TextView = itemView.findViewById(R.id.search_item_field2)
        val field2Icon: ImageView = itemView.findViewById(R.id.search_item_field2_icon)
        val field3: TextView = itemView.findViewById(R.id.search_item_field3)
        val field3Icon: ImageView = itemView.findViewById(R.id.search_item_field3_icon)
    }

    private class SearchCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) = oldItem === newItem

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) = oldItem == newItem
    }
}

