package com.ruideraj.backlog.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.ruideraj.backlog.R

class SearchLoadStateAdapter(private val retry: () -> Unit) : LoadStateAdapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): RecyclerView.ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_search_load_state, parent, false)
        return SearchLoadStateViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, loadState: LoadState) {
        val vh = holder as SearchLoadStateViewHolder

        vh.progress.isVisible = loadState is LoadState.Loading
        vh.text.isVisible = loadState is LoadState.Error
        vh.button.isVisible = loadState is LoadState.Error
        vh.button.setOnClickListener { retry.invoke() }

    }
}

class SearchLoadStateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    val progress: ProgressBar = itemView.findViewById(R.id.load_state_progress)
    val text: TextView = itemView.findViewById(R.id.load_state_text)
    val button: Button = itemView.findViewById(R.id.load_state_button)
}

