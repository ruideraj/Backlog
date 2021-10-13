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
import java.text.SimpleDateFormat

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

        companion object {
            private val MOVIE_DATE_FORMAT = SimpleDateFormat("yyyy")
        }

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
                    val filmData = searchResult.metadata as Metadata.FilmData

                    val directorPresent = filmData.director != null
                    field1Icon.isVisible = directorPresent
                    field1.isVisible = directorPresent
                    if (directorPresent) {
                        field1Icon.setImageResource(R.drawable.ic_person)
                        field1.text = filmData.director
                    }

                    val releaseDatePresent = filmData.releaseDate != null
                    field2Icon.isVisible = releaseDatePresent
                    field2.isVisible = releaseDatePresent
                    if (releaseDatePresent) {
                        field2.text = MOVIE_DATE_FORMAT.format(filmData.releaseDate)
                    }
                }
                MediaType.SHOW -> {
                    val showData = searchResult.metadata as Metadata.ShowData

                    field1Icon.isVisible = false
                    field1.isVisible = false

                    val runDatesPresent = showData.runDates != null
                    field2Icon.isVisible = runDatesPresent
                    field2.isVisible = runDatesPresent
                    if (runDatesPresent) {
                        field2.text = showData.runDates
                    }
                }
                MediaType.GAME -> {
                    val gameData = searchResult.metadata as Metadata.GameData

                    val developerPresent = gameData.developer != null
                    field1Icon.isVisible = developerPresent
                    field1.isVisible = developerPresent
                    if (developerPresent) {
                        field1Icon.setImageResource(R.drawable.ic_business)
                        field1.text = gameData.developer
                    }

                    val releaseDatePresent = gameData.releaseDate != null
                    field2Icon.isVisible = releaseDatePresent
                    field2.isVisible = releaseDatePresent
                    if (releaseDatePresent) {
                        field2.text = DateFormat.getDateInstance().format(gameData.releaseDate)
                    }
                }
                MediaType.BOOK -> {
                    val bookData = searchResult.metadata as Metadata.BookData

                    val authorPresent = bookData.author != null
                    field1Icon.isVisible = authorPresent
                    field1.isVisible = authorPresent
                    if (authorPresent) {
                        field1Icon.setImageResource(R.drawable.ic_person)
                        field1.text = bookData.author
                    }

                    val publishYearPresent = bookData.yearPublished != null
                    field2Icon.isVisible = publishYearPresent
                    field2.isVisible = publishYearPresent
                    bookData.yearPublished?.let { year ->
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

