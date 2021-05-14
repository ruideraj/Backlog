package com.ruideraj.backlog.entries

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.util.UpDownScrollListener
import com.ruideraj.backlog.util.asDp
import com.ruideraj.backlog.util.collectWhileStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntriesFragment : Fragment() {

    companion object {
        const val TAG = "EntriesFragment"
    }

    private val viewModel by viewModels<EntriesViewModel>()

    private lateinit var createFab: FloatingActionButton
    private lateinit var filmFab: FloatingActionButton
    private lateinit var tvFab: FloatingActionButton
    private lateinit var gameFab: FloatingActionButton
    private lateinit var bookFab: FloatingActionButton
    private lateinit var menuOverlay: View

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewModel.onBackPressed()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entries, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val list = requireArguments().getParcelable<BacklogList>(Constants.ARG_LIST)
            ?: throw IllegalStateException("Need to provide a list to Entries screen")

        (requireActivity() as AppCompatActivity).supportActionBar?.title = list.title

        val recycler = view.findViewById<RecyclerView>(R.id.entries_recycler).apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        }

        val adapter = EntriesAdapter(viewModel)
        recycler.adapter = adapter

        createFab = view.findViewById<FloatingActionButton>(R.id.entries_button_create).apply {
            setOnClickListener { viewModel.onClickCreateButton() }

            viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewModel.showCreateMenu.observe(viewLifecycleOwner) { show ->
                        if (show) {
                            expandFabMenu()
                        } else {
                            collapseFabMenu()
                        }

                        backPressedCallback.isEnabled = show
                    }

                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        filmFab = view.findViewById<FloatingActionButton>(R.id.entries_button_film).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.FILM) }
        }
        tvFab = view.findViewById<FloatingActionButton>(R.id.entries_button_tv).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.TV) }
        }
        gameFab = view.findViewById<FloatingActionButton>(R.id.entries_button_game).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.GAME) }
        }
        bookFab = view.findViewById<FloatingActionButton>(R.id.entries_button_book).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.BOOK) }
        }
        menuOverlay = view.findViewById(R.id.entries_menu_overlay)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        recycler.addOnScrollListener(UpDownScrollListener({
            showFabMenu()
        }, {
            hideFabMenu()
        }))

        viewModel.let {
            it.entries.observe(viewLifecycleOwner) { entriesList ->
                adapter.submitList(entriesList)
            }

            it.eventFlow.collectWhileStarted(viewLifecycleOwner) { event ->
                when (event) {
                    is EntriesViewModel.Event.GoToEntryCreate -> {
                        val directions = EntriesFragmentDirections
                            .actionEntriesFragmentToEntriesEditFragment(event.type)
                        findNavController().navigate(directions)
                    }
                }

            }
        }

        viewModel.loadEntries(list.id)
    }

    private fun showFabMenu() {
        createFab.show()
        filmFab.show()
        tvFab.show()
        gameFab.show()
        bookFab.show()
    }

    private fun hideFabMenu() {
        createFab.hide()
        filmFab.hide()
        tvFab.hide()
        gameFab.hide()
        bookFab.hide()
    }

    private fun expandFabMenu() {
        val fabDifference = - (createFab.height - filmFab.height)

        val resources = resources
        val height = filmFab.height
        val margin = 16.asDp(resources)

        val menuFabHeight = -margin - height

        createFab.animate().rotationBy(45f)
        filmFab.apply {
            visibility = View.VISIBLE
            animate().translationYBy(fabDifference + menuFabHeight)
        }
        tvFab.apply {
            visibility = View.VISIBLE
            animate().translationYBy(fabDifference + menuFabHeight * 2)
        }
        gameFab.apply {
            visibility = View.VISIBLE
            animate().translationYBy(fabDifference + menuFabHeight * 3)
        }
        bookFab.apply {
            visibility = View.VISIBLE
            animate().translationYBy(fabDifference + menuFabHeight * 4)
        }
        menuOverlay.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate().alpha(1f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    alpha = 1f
                }
            })
        }
    }

    private fun collapseFabMenu() {
        createFab.animate().rotation(0f)
        filmFab.animate().translationY(0f)
        tvFab.animate().translationY(0f)
        gameFab.animate().translationY(0f)
        bookFab.animate().translationY(0f)
        menuOverlay.apply {
            animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator?) {
                    visibility = View.GONE
                    filmFab.visibility = View.GONE
                    tvFab.visibility = View.GONE
                    gameFab.visibility = View.GONE
                    bookFab.visibility = View.GONE
                }
            })
        }
    }

}