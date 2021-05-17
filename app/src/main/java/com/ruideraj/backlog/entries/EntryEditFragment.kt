package com.ruideraj.backlog.entries

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.util.EntryField
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryEditFragment : Fragment() {

    companion object {
        private const val TAG = "EntryEditFragment"
    }

    private val viewModel by viewModels<EntryEditViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val type = requireArguments().getSerializable(Constants.ARG_TYPE) as MediaType
        Log.d(TAG, "type: $type")

        val title = getString(R.string.entry_title, type.name.toLowerCase().capitalize())
        (requireActivity() as AppCompatActivity).supportActionBar?.title = title

        viewModel.setType(type)

        viewModel.let {
            it.fields.observe(viewLifecycleOwner, { shownFields ->
                val fieldLayout = view.findViewById<LinearLayout>(R.id.entry_field_layout)

                val dateField = fieldLayout.findViewById<EntryField>(R.id.entry_field_date)
                setFieldVisibilityAndHint(dateField, shownFields.releaseDate)

                val creator1Field = fieldLayout.findViewById<EntryField>(R.id.entry_field_creator1)
                setFieldVisibilityAndHint(creator1Field, shownFields.creator1)

                val creator2Field = fieldLayout.findViewById<EntryField>(R.id.entry_field_creator2)
                setFieldVisibilityAndHint(creator2Field, shownFields.creator2)
            })
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