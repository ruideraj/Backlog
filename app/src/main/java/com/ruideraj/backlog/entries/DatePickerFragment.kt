package com.ruideraj.backlog.entries

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import java.util.*

class DatePickerFragment : DialogFragment() {

    private val viewModel: EntryEditViewModel by viewModels(ownerProducer = {requireParentFragment()})

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val listener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
            viewModel.onDateSelected(year, month, day)
        }

        return DatePickerDialog(requireContext(), listener, currentYear, currentMonth, currentDay)
    }

}