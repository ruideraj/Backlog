package com.ruideraj.backlog.util

import androidx.recyclerview.widget.RecyclerView

class UpDownScrollListener(private val onScrollUp: () -> Unit,
                           private val onScrollDown: () -> Unit) : RecyclerView.OnScrollListener() {

    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
        if (dy > 0) {
            onScrollDown.invoke()
        } else if (dy < 0) {
            onScrollUp.invoke()
        }
    }
}