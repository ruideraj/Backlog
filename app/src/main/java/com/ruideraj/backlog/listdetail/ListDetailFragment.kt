package com.ruideraj.backlog.listdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.R
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListDetailFragment : Fragment() {

    companion object {
        const val TAG = "ListDetailFragment"
    }

    private val viewModel by viewModels<ListDetailViewModel>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_list_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = requireArguments().getParcelable<BacklogList>(Constants.ARG_LIST)
            ?: throw IllegalStateException("Need to provide a list to List Detail screen")

        (requireActivity() as AppCompatActivity).supportActionBar?.title = list.title

        viewModel.loadEntries(list.id)
    }

}