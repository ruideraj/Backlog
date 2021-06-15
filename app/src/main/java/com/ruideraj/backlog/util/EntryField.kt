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

    private lateinit var entryEditText: TextInputEditText

    private fun initView() {
        val view = inflate(context, R.layout.view_entry_field, this)
        entryEditText = view.findViewById(R.id.entry_field_edit)
    }

    fun getText() = entryEditText.editableText.toString()

    fun setText(string: String?) = entryEditText.setText(string)

}