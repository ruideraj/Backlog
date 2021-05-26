package com.ruideraj.backlog.entries

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputEditText
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.util.EntryField
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryEditFragment : Fragment() {

    companion object {
        private const val TAG = "EntryEditFragment"
        private const val DATE_DIALOG_TAG = "DateDialog"
    }

    private val viewModel by viewModels<EntryEditViewModel>()

    private lateinit var titleField: EntryField
    private lateinit var imageField: EntryField
    private lateinit var dateField: EntryField
    private lateinit var creator1Field: EntryField
    private lateinit var creator2Field: EntryField

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = requireArguments().getSerializable(Constants.ARG_TYPE) as MediaType
        Log.d(TAG, "type: $type")

        val title = getString(R.string.entry_title, type.name.toLowerCase().capitalize())
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

        titleField = view.findViewById<EntryField>(R.id.entry_field_title).apply {
            findViewById<TextInputEditText>(R.id.entry_field_edit).apply {
                addTextChangedListener { editable ->
                    viewModel.onTitleTextChanged(editable.toString())
                }
            }
        }
        imageField = view.findViewById(R.id.entry_field_image_url)
        dateField = view.findViewById<EntryField>(R.id.entry_field_date).apply {
            findViewById<TextInputEditText>(R.id.entry_field_edit).apply {
                isClickable = true
                isLongClickable = false
                isFocusableInTouchMode = false
                inputType = InputType.TYPE_CLASS_DATETIME
                setOnClickListener {
                    DatePickerFragment().apply {
                        show(this@EntryEditFragment.childFragmentManager, DATE_DIALOG_TAG)
                    }
                }
            }
        }
        creator1Field = view.findViewById(R.id.entry_field_creator1)
        creator2Field = view.findViewById(R.id.entry_field_creator2)

        viewModel.setType(type)

        viewModel.let {
            it.fields.observe(viewLifecycleOwner, { shownFields ->
                setFieldVisibilityAndHint(dateField, shownFields.releaseDate)
                setFieldVisibilityAndHint(creator1Field, shownFields.creator1)
                setFieldVisibilityAndHint(creator2Field, shownFields.creator2)
            })

            it.titleError.observe(viewLifecycleOwner, { error ->
                if (error) {
                    titleField.error = getString(R.string.error_title)
                } else {
                    titleField.error = null
                }
            })

            it.releaseDate.observe(viewLifecycleOwner, { releaseDate ->
                dateField.text = releaseDate
            })
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_entry_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.entry_action_confirm -> {
                viewModel.onClickConfirm(titleField.text, imageField.text, creator1Field.text, creator2Field.text)
                return true
            }
            R.id.entry_action_search -> {
                // TODO
                return true
            }
            else -> false
        }
    }

    private fun setFieldVisibilityAndHint(entryField: EntryField, hintTextRes: Int) {
        if (hintTextRes < 0) {
            entryField.visibility = View.GONE
        } else {
            entryField.setHint(hintTextRes)
            entryField.visibility = View.VISIBLE
        }
    }
}