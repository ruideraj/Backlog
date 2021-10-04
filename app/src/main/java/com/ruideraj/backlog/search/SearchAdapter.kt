package com.ruideraj.backlog.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.Metadata
import com.ruideraj.backlog.R
import com.ruideraj.backlog.SearchResult
import java.text.DateFormat

class SearchAdapter(private val onItemClick: (SearchResult) -> Unit)
    : PagingDataAdapter<SearchResult, RecyclerView.ViewHolder>(SearchCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ViewHolder(inflater.inflate(R.layout.item_search_result, parent, false), onItemClick)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val vh = holder as ViewHolder
        val searchResult = getItem(position) ?: return

        vh.bind(searchResult)
    }

    private class ViewHolder(itemView: View,
                             private val onClick: (searchResult: SearchResult) -> Unit)
        : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.search_item_title)
        private val image: ImageView = itemView.findViewById(R.id.search_item_image)
        private val field1: TextView = itemView.findViewById(R.id.search_item_field1)
        private val field1Icon: ImageView = itemView.findViewById(R.id.search_item_icon1)
        private val field2: TextView = itemView.findViewById(R.id.search_item_field2)
        private val field2Icon: ImageView = itemView.findViewById(R.id.search_item_icon2)
        private val field3: TextView = itemView.findViewById(R.id.search_item_field3)
        private val field3Icon: ImageView = itemView.findViewById(R.id.search_item_icon3)

        fun bind(searchResult: SearchResult) {
            title.text = searchResult.title

            field2Icon.setImageResource(R.drawable.ic_event)

            Glide.with(image.context)
                .load(searchResult.metadata.imageUrl)
                .circleCrop()
                .placeholder(getImageForType(searchResult.type))
                .into(image)

            // This field currently unused by all media types
            field3.visibility = View.GONE
            field3Icon.visibility = View.GONE

            when (searchResult.type) {
                MediaType.FILM -> {

                }
                MediaType.SHOW -> {

                }
                MediaType.GAME -> {
                    val metadata = searchResult.metadata as Metadata.GameData

                    val developerPresent = metadata.developer != null
                    field1Icon.isVisible = developerPresent
                    field1.isVisible = developerPresent
                    if (developerPresent) {
                        field1Icon.setImageResource(R.drawable.ic_business)
                        field1.text = metadata.developer
                    }

                    val releaseDatePresent = metadata.releaseDate != null
                    field2Icon.isVisible = releaseDatePresent
                    field2.isVisible = releaseDatePresent
                    if (releaseDatePresent) {
                        field2.text = DateFormat.getDateInstance().format(metadata.releaseDate)
                    }
                }
                MediaType.BOOK -> {
                    val metadata = searchResult.metadata as Metadata.BookData

                    val authorPresent = metadata.author != null
                    field1Icon.isVisible = authorPresent
                    field1.isVisible = authorPresent
                    if (authorPresent) {
                        field1Icon.setImageResource(R.drawable.ic_person)
                        field1.text = metadata.author
                    }

                    val publishYearPresent = metadata.yearPublished != null
                    field2Icon.isVisible = publishYearPresent
                    field2.isVisible = publishYearPresent
                    metadata.yearPublished?.let { year ->
                        field2.run { text = context.getString(R.string.search_field_published, year.value) }
                    }
                }
            }

            itemView.setOnClickListener { onClick.invoke(searchResult) }
        }

        private fun getImageForType(type: MediaType): Int {
            return when (type) {
                MediaType.FILM -> R.drawable.ic_film_40
                MediaType.SHOW -> R.drawable.ic_show_40
                MediaType.GAME -> R.drawable.ic_game_40
                MediaType.BOOK -> R.drawable.ic_book_40
            }
        }
    }

    private class SearchCallback : DiffUtil.ItemCallback<SearchResult>() {
        override fun areItemsTheSame(oldItem: SearchResult, newItem: SearchResult) = oldItem === newItem

        override fun areContentsTheSame(oldItem: SearchResult, newItem: SearchResult) = oldItem == newItem
    }
}

