package com.ruideraj.backlog.lists

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.ListIcon
import com.ruideraj.backlog.R
import com.udit.android.flatradiogroup.FlatRadioGroup

class ListDialogFragment : DialogFragment() {

    private lateinit var viewModel: ListsViewModel

    private lateinit var createdView: View
    private lateinit var titleInputLayout: TextInputLayout

    override fun onAttach(context: Context) {
        super.onAttach(context)
        viewModel = requireParentFragment().viewModels<ListsViewModel>().value
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        createdView = requireActivity().layoutInflater.inflate(R.layout.dialog_list, null)

        val args = requireArguments()

        val mode = args.getInt(Constants.ARG_MODE, Constants.MODE_CREATE)
        val dialogTitleId = if (mode == Constants.MODE_EDIT) R.string.lists_edit_list else R.string.lists_create_list

        val initialTitle = args.getString(Constants.ARG_TITLE)
        if (!initialTitle.isNullOrBlank()) {
            createdView.findViewById<EditText>(R.id.list_edit_title_edit).setText(initialTitle)
        }

        val checkedId = getButtonIdForIcon(args.getSerializable(Constants.ARG_ICON) as ListIcon)
        val flatRadioGroup = createdView.findViewById<FlatRadioGroup>(R.id.list_edit_radio_group)
        flatRadioGroup.selectViewProgramatically(checkedId)

        val positiveButtonTextId = if (mode == Constants.MODE_EDIT) R.string.edit else R.string.create

        return AlertDialog.Builder(requireContext()).apply {
            setTitle(dialogTitleId)
            setView(createdView)
            setPositiveButton(positiveButtonTextId) { _, _ ->
                // Do nothing.  Need to have empty handler here to instantiate the button.
            }
            setNegativeButton(R.string.cancel) { _, _ ->
                // Do nothing, dialog should be automatically dismissed
            }
        }.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = createdView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel.let {
            view.findViewById<EditText>(R.id.list_edit_title_edit).addTextChangedListener { editable ->
                it.onDialogTitleTextChanged(editable.toString())
            }

            titleInputLayout = view.findViewById(R.id.list_edit_title_layout)
            it.showListDialogTitleError.observe(viewLifecycleOwner) { showError ->
                if (showError) {
                    titleInputLayout.error = getString(R.string.lists_dialog_error_title)
                } else {
                    titleInputLayout.isErrorEnabled = false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        (dialog as? AlertDialog)?.let {
            // Set positive button listener after dialog is shown to prevent automatically closing the dialog
            // when the button is clicked.
            it.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val args = requireArguments()
                val mode = args.getInt(Constants.ARG_MODE, Constants.MODE_CREATE)

                val title = getTitle(createdView)
                val selectedIcon = getSelectedIcon(createdView)

                if (mode == Constants.MODE_EDIT) {
                    val listId = args.getLong(Constants.ARG_LIST_ID, -1)
                    viewModel.editList(listId, title, selectedIcon)
                } else {
                    viewModel.createList(title, selectedIcon)
                }
            }
        }
    }

    private fun getTitle(view: View): String = view.findViewById<EditText>(R.id.list_edit_title_edit).text.toString()

    private fun getSelectedIcon(view: View): ListIcon {
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_film).isChecked) return ListIcon.FILM
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_tv).isChecked) return ListIcon.TV
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_game).isChecked) return ListIcon.GAME
        if (view.findViewById<RadioButton>(R.id.list_edit_radio_book).isChecked) return ListIcon.BOOK

        return ListIcon.LIST
    }

    private fun getButtonIdForIcon(icon: ListIcon): Int {
        return when (icon) {
            ListIcon.LIST -> R.id.list_edit_radio_list
            ListIcon.FILM -> R.id.list_edit_radio_film
            ListIcon.TV -> R.id.list_edit_radio_tv
            ListIcon.GAME -> R.id.list_edit_radio_game
            ListIcon.BOOK -> R.id.list_edit_radio_book
        }
    }

}