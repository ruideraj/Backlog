package com.ruideraj.backlog.lists

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.R

class DeleteListDialogFragment : DialogFragment() {

    companion object {
        const val ARG_LIST = "list"
    }

    private lateinit var viewModel: ListsViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = requireParentFragment().viewModels<ListsViewModel>().value
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val listToDelete = requireArguments().getParcelable<BacklogList>(ARG_LIST)!!

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(R.string.lists_delete_list)
            setMessage(getString(R.string.lists_delete_confirm, listToDelete.title))
            setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteList(listToDelete.id)
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, dialog should be automatically dismissed
            }
        }.create()
    }
}