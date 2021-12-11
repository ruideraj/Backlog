package com.ruideraj.backlog.util

import android.content.Context
import android.text.InputType
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

    var text: String?
        set(value) = editText!!.setText(value)
        get() = editText!!.editableText.toString()

    init {
        // Using getContext() instead of Context in constructor args because
        // using contructor Context sets a background on the TextInputEditText in the inflated layout.
        // Inflating with the one from getContext() leaves the background null,
        // allowing for the custom styling defined through defStyleAttr.
        // This behavior is determined by TextInputLayout.shouldUseEditTextBackgroundForBoxBackground()
        inflate(getContext(), R.layout.view_entry_field, this)

        context.theme.obtainStyledAttributes(attrs, R.styleable.EntryField, 0, 0).apply {
            val inputType = getInteger(R.styleable.EntryField_android_inputType, InputType.TYPE_CLASS_TEXT)
            editText!!.setRawInputType(inputType)

            val text = getString(R.styleable.EntryField_android_text)
            editText!!.setText(text)
        }
    }

}