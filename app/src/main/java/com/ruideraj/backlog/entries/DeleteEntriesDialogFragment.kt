package com.ruideraj.backlog.entries

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.R

class DeleteEntriesDialogFragment : DialogFragment() {

    private val viewModel: EntriesViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val count = requireArguments().getInt(Constants.ARG_COUNT)

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.entries_delete_title)
            setMessage(getString(R.string.entries_delete_confirm, count))
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.onConfirmDelete()
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, dialog should be automatically dismissed
            }
        }.create()
    }

}