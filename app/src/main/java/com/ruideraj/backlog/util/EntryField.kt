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

    private var entryEditText: TextInputEditText

    var text: String?
        set(value) = entryEditText.setText(value)
        get() = entryEditText.editableText.toString()

    init {
        // Using getContext() instead of Context in constructor args because
        // using contructor Context sets a background on the TextInputEditText in the inflated layout.
        // Inflating with the one from getContext() leaves the background null,
        // allowing for the custom styling defined through defStyleAttr.
        // This behavior is determined by TextInputLayout.shouldUseEditTextBackgroundForBoxBackground()
        inflate(getContext(), R.layout.view_entry_field, this)
        entryEditText = findViewById(R.id.entry_field_edit)

        context.theme.obtainStyledAttributes(attrs, R.styleable.EntryField, 0, 0).apply {
            val inputType = getInteger(R.styleable.EntryField_android_inputType, InputType.TYPE_CLASS_TEXT)
            entryEditText.setRawInputType(inputType)

            val text = getString(R.styleable.EntryField_android_text)
            entryEditText.setText(text)
        }
    }

}