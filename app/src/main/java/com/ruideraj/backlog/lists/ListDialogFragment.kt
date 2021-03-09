package com.ruideraj.backlog.lists

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.R

class ListDialogFragment : DialogFragment() {

    private lateinit var viewModel: ListsViewModel

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_list, null)

        return AlertDialog.Builder(requireContext()).apply {
            setView(view)
            setPositiveButton(R.string.lists_create) { _, _ ->
                viewModel.createList(getTitle(view), getCheckedRadioButtonId(view))
            }
            setNegativeButton(R.string.lists_cancel) { _, _ ->
                // Do nothing, dialog should be automatically dismissed
            }
        }.create()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = requireParentFragment().viewModels<ListsViewModel>().value
    }

    private fun getTitle(view: View): String = view.findViewById<EditText>(R.id.list_edit_title_edit).text.toString()

    private fun getCheckedRadioButtonId(view: View): ListIcon {
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_film).isChecked) return ListIcon.FILM
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_tv).isChecked) return ListIcon.TV
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_game).isChecked) return ListIcon.GAME
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_book).isChecked) return ListIcon.BOOK

        return ListIcon.LIST
    }

}