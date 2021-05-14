package com.ruideraj.backlog.util

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.AttrRes
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.ruideraj.backlog.R

class EntryField @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    @AttrRes defStyleAttr: Int = R.attr.entryFieldStyle)
    : TextInputLayout(context, attrs, defStyleAttr) {

    init {
        initView()
    }

    private val entryEditText = findViewById<TextInputEditText>(R.id.entry_field_edit)

    private fun initView() {
        inflate(context, R.layout.view_entry_field, this)
    }

    fun getText() = entryEditText.editableText.toString()

}