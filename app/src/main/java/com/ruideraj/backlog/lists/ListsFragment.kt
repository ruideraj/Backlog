package com.ruideraj.backlog.lists

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.R
import com.ruideraj.backlog.ViewModelFactory

class ListsFragment : Fragment() {

    companion object {
        private const val TAG = "ListsFragment"
    }

    private val viewModel by viewModels<ListsViewModel> { ViewModelFactory(requireActivity()) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_lists, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as AppCompatActivity).supportActionBar?.title = getString(R.string.app_name)

        view.findViewById<FloatingActionButton>(R.id.lists_button_create).setOnClickListener {
            // TODO Open Create List dialog
        }

        viewModel.let {
            it.lists.observe(requireActivity(), { lists ->
                Toast.makeText(requireContext(), "Lists: ${lists.size}", Toast.LENGTH_SHORT).show()
            })
        }
    }

}