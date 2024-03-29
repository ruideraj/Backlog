package com.ruideraj.backlog.entries

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.iterator
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.ruideraj.backlog.BacklogList
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.lists.DragDropHelperCallback
import com.ruideraj.backlog.lists.ScrollOnAddObserver
import com.ruideraj.backlog.util.asDp
import com.ruideraj.backlog.util.collectWhileStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntriesFragment : Fragment() {

    companion object {
        const val TAG = "EntriesFragment"
        const val DELETE_DIALOG_TAG = "DeleteEntriesDialog"

        private const val MENU_DELETE_MODE = 0
        private const val MENU_DELETE = 1
    }

    private val viewModel by viewModels<EntriesViewModel>()

    private lateinit var toolbar: Toolbar
    private lateinit var createFab: FloatingActionButton
    private lateinit var filmFab: FloatingActionButton
    private lateinit var showFab: FloatingActionButton
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

        val args = requireArguments()
        val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            args.getParcelable(Constants.ARG_LIST, BacklogList::class.java)
        } else {
            args.getParcelable(Constants.ARG_LIST)
        }
        if (list == null) throw IllegalStateException("Need to provide a list to Entries screen")

        toolbar = view.findViewById<Toolbar>(R.id.entries_toolbar).apply {
            addMenuProvider(EntriesMenuProvider())
            setNavigationOnClickListener {
                viewModel.onClickNavigationIcon()
            }
        }

        val recycler = view.findViewById<RecyclerView>(R.id.entries_recycler).apply {
            val linearLayoutManager = LinearLayoutManager(view.context, LinearLayoutManager.VERTICAL, false)
            layoutManager = linearLayoutManager

            val spaceDivider = DividerItemDecoration(context, linearLayoutManager.orientation).apply {
                ContextCompat.getDrawable(context, R.drawable.divider_space)?.let { setDrawable(it) }
            }
            addItemDecoration(spaceDivider)
        }

        val adapter = EntriesAdapter(viewModel).apply {
            registerAdapterDataObserver(ScrollOnAddObserver(recycler))
        }
        recycler.adapter = adapter

        val dragDropCallback = DragDropHelperCallback(adapter, { dragStartPosition ->
            viewModel.moveEntryStarted(dragStartPosition)
        }) { dragEndPosition ->
            viewModel.moveEntryEnded(dragEndPosition)
        }
        ItemTouchHelper(dragDropCallback).attachToRecyclerView(recycler)

        createFab = view.findViewById<FloatingActionButton>(R.id.entries_button_create).apply {
            setOnClickListener { viewModel.onClickCreateButton() }

            viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    viewModel.expandCreateMenu.observe(viewLifecycleOwner) { expand ->
                        if (expand) {
                            expandFabMenu()
                        } else {
                            collapseFabMenu()
                        }
                    }

                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
        filmFab = view.findViewById<FloatingActionButton>(R.id.entries_button_film).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.FILM) }
        }
        showFab = view.findViewById<FloatingActionButton>(R.id.entries_button_show).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.SHOW) }
        }
        gameFab = view.findViewById<FloatingActionButton>(R.id.entries_button_game).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.GAME) }
        }
        bookFab = view.findViewById<FloatingActionButton>(R.id.entries_button_book).apply {
            setOnClickListener { viewModel.onClickCreateMenuButton(MediaType.BOOK) }
        }
        menuOverlay = view.findViewById(R.id.entries_menu_overlay)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backPressedCallback)

        viewModel.let {
            it.title.observe(viewLifecycleOwner) { title ->
                toolbar.title = title
            }

            it.selectMode.observe(viewLifecycleOwner) { selectMode ->
                toolbar.apply {
                    invalidateMenu()

                    val navIcon = if (selectMode) R.drawable.ic_close else R.drawable.ic_arrow_back
                    setNavigationIcon(navIcon)
                }

                val enabledDragDirections = if (selectMode) 0 else ItemTouchHelper.UP or ItemTouchHelper.DOWN
                dragDropCallback.setDefaultDragDirs(enabledDragDirections)
            }

            it.entries.observe(viewLifecycleOwner) { entriesList ->
                adapter.submitList(entriesList)
            }

            it.showCreateMenu.observe(viewLifecycleOwner) { show ->
                if (show) {
                    showFabMenu()
                } else {
                    hideFabMenu()
                }
            }

            it.backPressedCallbackEnabled.observe(viewLifecycleOwner) { enabled ->
                backPressedCallback.isEnabled = enabled
            }

            it.eventFlow.collectWhileStarted(viewLifecycleOwner) { event ->
                when (event) {
                    is EntriesViewModel.Event.NavigateUp -> findNavController().navigateUp()
                    is EntriesViewModel.Event.GoToEntryCreate -> {
                        val directions = EntriesFragmentDirections
                            .actionEntriesFragmentToEntriesEditFragment(list.id, event.type, null)
                        findNavController().navigate(directions)
                    }
                    is EntriesViewModel.Event.GoToEntryView -> {
                        val directions = EntriesFragmentDirections
                            .actionEntriesFragmentToEntriesEditFragment(list.id, event.type, event.entry)
                        findNavController().navigate(directions)
                    }
                    is EntriesViewModel.Event.EntrySelectedChanged -> {
                        adapter.notifyItemChanged(event.position)
                    }
                    is EntriesViewModel.Event.SelectedEntriesCleared -> {
                        adapter.notifyDataSetChanged()
                    }
                    is EntriesViewModel.Event.ShowDeleteConfirmation -> {
                        val args = Bundle().apply { putInt(Constants.ARG_COUNT, event.count) }
                        DeleteEntriesDialogFragment().let { dialog ->
                            dialog.arguments = args
                            dialog.show(childFragmentManager, DELETE_DIALOG_TAG)
                        }
                    }
                }
            }
        }

        viewModel.loadEntries(list)
    }

    private fun showFabMenu() {
        createFab.show()
    }

    private fun hideFabMenu() {
        createFab.hide()
    }

    private fun expandFabMenu() {
        val fabDifference = -(createFab.height - filmFab.height)

        val resources = resources
        val height = filmFab.height
        val margin = 16.asDp(resources)

        val menuFabHeight = -margin - height

        createFab.animate().rotationBy(45f)
        filmFab.apply {
            visibility = View.VISIBLE
            animate().translationYBy(fabDifference + menuFabHeight)
        }
        showFab.apply {
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
                override fun onAnimationEnd(animation: Animator) {
                    alpha = 1f
                }
            })
        }
    }

    private fun collapseFabMenu() {
        createFab.animate().rotation(0f)
        filmFab.animate().translationY(0f)
        showFab.animate().translationY(0f)
        gameFab.animate().translationY(0f)
        bookFab.animate().translationY(0f)
        menuOverlay.apply {
            animate().alpha(0f).setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    visibility = View.GONE
                    filmFab.visibility = View.GONE
                    showFab.visibility = View.GONE
                    gameFab.visibility = View.GONE
                    bookFab.visibility = View.GONE
                }
            })
        }
    }

    private inner class EntriesMenuProvider : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.menu_entries_select, menu)
        }

        override fun onPrepareMenu(menu: Menu) {
            val isSelectMode = viewModel.selectMode.value == true
            menu.iterator().forEach { menuItem ->
                when (menuItem.itemId) {
                    R.id.entries_select_delete_mode -> menuItem.isVisible = !isSelectMode
                    R.id.entries_select_delete -> menuItem.isVisible = isSelectMode
                }
            }
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when (menuItem.itemId) {
                R.id.entries_select_delete_mode -> {
                    viewModel.onClickDeleteMode()
                    return true
                }
                R.id.entries_select_delete -> {
                    viewModel.onClickDelete()
                    return true
                }
            }

            return false
        }
    }
}