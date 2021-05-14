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
import com.ruideraj.backlog.util.asDp
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
            it.fields.observe(viewLifecycleOwner, { fields ->
                val fieldLayout = view.findViewById<LinearLayout>(R.id.entry_edit_field_layout)

                fields.forEach { field ->
                    val entryField = EntryField(view.context, null, R.attr.entryFieldStyle)
                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                            topMargin = 16.asDp(resources).toInt()
                    }
                    entryField.layoutParams = layoutParams
                    entryField.hint = field
                    fieldLayout.addView(entryField)
                }
            })
        }
    }

}