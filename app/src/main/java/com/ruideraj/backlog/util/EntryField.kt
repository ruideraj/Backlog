package com.ruideraj.backlog.util

import android.content.Context
import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import android.text.InputType
import android.util.AttributeSet
import androidx.annotation.AttrRes
import androidx.annotation.RequiresApi
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
            this@EntryField.text = text
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()

        val state = SavedState(superState)
        state.inputText = text

        return state
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val savedState = state as SavedState

        super.onRestoreInstanceState(savedState.superState)

        text = savedState.inputText
    }

    private class SavedState : BaseSavedState {
        var inputText: String? = null

        constructor(superState: Parcelable?) : super(superState)

        constructor(inState: Parcel) : super(inState) {
            inputText = inState.readString()
        }

        @RequiresApi(Build.VERSION_CODES.N)
        constructor(inState: Parcel, classLoader: ClassLoader) : super(inState, classLoader) {
            inputText = inState.readString()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeString(inputText)
        }

        companion object CREATOR : Parcelable.ClassLoaderCreator<SavedState> {
            override fun createFromParcel(parcel: Parcel): SavedState {
                return SavedState(parcel)
            }

            override fun createFromParcel(parcel: Parcel, loader: ClassLoader) : SavedState {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    SavedState(parcel, loader)
                } else SavedState(parcel)
            }

            override fun newArray(size: Int): Array<SavedState?> {
                return arrayOfNulls(size)
            }
        }
    }
}