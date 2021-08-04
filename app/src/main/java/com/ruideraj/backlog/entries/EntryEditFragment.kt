package com.ruideraj.backlog.entries

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.InputType
import android.view.*
import android.widget.ImageView
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.textfield.TextInputEditText
import com.ruideraj.backlog.Constants
import com.ruideraj.backlog.Entry
import com.ruideraj.backlog.MediaType
import com.ruideraj.backlog.R
import com.ruideraj.backlog.util.EntryField
import com.ruideraj.backlog.util.collectWhileStarted
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryEditFragment : Fragment() {

    companion object {
        private const val TAG = "EntryEditFragment"
        private const val DATE_DIALOG_TAG = "DateDialog"

        private const val MENU_EDIT = 0
        private const val MENU_SEARCH = 1
        private const val MENU_CONFIRM = 2
    }

    private val viewModel by viewModels<EntryEditViewModel>()

    private lateinit var toolbar: Toolbar
    private lateinit var titleField: EntryField
    private lateinit var imageField: EntryField
    private lateinit var dateField: EntryField
    private lateinit var creator1Field: EntryField
    private lateinit var creator2Field: EntryField

    private lateinit var imageThumbnail: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_entry_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = requireArguments()

        val listId = args.getLong(Constants.ARG_LIST_ID, -1L)
        val type = args.getSerializable(Constants.ARG_TYPE) as MediaType
        val entry = args.getParcelable(Constants.ARG_ENTRY) as Entry?

        viewModel.initialize(listId, type, entry)

        toolbar = view.findViewById<Toolbar>(R.id.entry_toolbar).apply {
            setNavigationOnClickListener { viewModel.onClickNavigationIcon() }
            inflateMenu(R.menu.menu_entry_edit)
            menu.apply {
                getItem(MENU_EDIT).setOnMenuItemClickListener {
                    viewModel.onClickEditMode()
                    true
                }
                getItem(MENU_SEARCH).setOnMenuItemClickListener {
                    viewModel.onClickSearch()
                    true
                }
                getItem(MENU_CONFIRM).setOnMenuItemClickListener {
                    viewModel.submit(
                        titleField.getText(),
                        imageField.getText(),
                        creator1Field.getText(),
                        creator2Field.getText()
                    )
                    true
                }
            }
        }

        titleField = view.findViewById<EntryField>(R.id.entry_field_title).apply {
            findViewById<TextInputEditText>(R.id.entry_field_edit).apply {
                addTextChangedListener { editable ->
                    viewModel.onTitleTextChanged(editable.toString())
                }
            }
        }

        imageThumbnail = view.findViewById(R.id.entry_field_image)
        imageField = view.findViewById<EntryField>(R.id.entry_field_image_url).apply {
            editText?.addTextChangedListener { editable ->
                viewModel.onImageTextChanged(editable.toString())
            }
        }

        dateField = view.findViewById<EntryField>(R.id.entry_field_date).apply {
            findViewById<TextInputEditText>(R.id.entry_field_edit).apply {
                isClickable = true
                isLongClickable = false
                isFocusableInTouchMode = false
                inputType = InputType.TYPE_CLASS_DATETIME
                setOnClickListener {
                    DatePickerFragment().apply {
                        show(this@EntryEditFragment.childFragmentManager, DATE_DIALOG_TAG)
                    }
                }
            }
        }

        creator1Field = view.findViewById(R.id.entry_field_creator1)
        creator2Field = view.findViewById(R.id.entry_field_creator2)

        viewModel.let {
            it.title.observe(viewLifecycleOwner, { title ->
                toolbar.title = title
            })

            it.showCloseIcon.observe(viewLifecycleOwner, { show ->
                if (show) {
                    toolbar.setNavigationIcon(R.drawable.ic_close)
                } else {
                    toolbar.setNavigationIcon(R.drawable.ic_arrow_back)
                }
            })

            it.editMode.observe(viewLifecycleOwner, { editMode ->
                toolbar.menu.apply {
                    getItem(MENU_SEARCH).isVisible = editMode
                    getItem(MENU_CONFIRM).isVisible = editMode
                }
                titleField.isEnabled = editMode
                imageField.isEnabled = editMode
                dateField.isEnabled = editMode
                creator1Field.isEnabled = editMode
                creator2Field.isEnabled = editMode
            })

            it.showEditModeAction.observe(viewLifecycleOwner, { show ->
                toolbar.menu.getItem(MENU_EDIT).isVisible = show
            })

            it.fields.observe(viewLifecycleOwner, { shownFields ->
                setFieldVisibilityAndHint(dateField, shownFields.releaseDate)
                setFieldVisibilityAndHint(creator1Field, shownFields.creator1)
                setFieldVisibilityAndHint(creator2Field, shownFields.creator2)
            })

            it.titleError.observe(viewLifecycleOwner, { error ->
                if (error) {
                    titleField.error = getString(R.string.error_title)
                } else {
                    titleField.error = null
                }
            })

            it.imageUrl.observe(viewLifecycleOwner, { imageInput ->
                if (imageInput.isNullOrBlank()) {
                    Glide.with(this).clear(imageThumbnail)
                } else {
                    Glide.with(this)
                        .load(imageInput)
                        .circleCrop()
                        .addListener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?,
                                                      isFirstResource: Boolean): Boolean {
                                viewModel.onImageLoadError()
                                return false
                            }

                            override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?,
                                                         dataSource: DataSource?, isFirstResource: Boolean): Boolean = false
                        })
                        .into(imageThumbnail)
                }
            })

            it.imageError.observe(viewLifecycleOwner, { error ->
                if (error) {
                    imageField.error = getString(R.string.error_image)
                } else {
                    imageField.error = null
                }
            })

            it.releaseDate.observe(viewLifecycleOwner, { releaseDate ->
                dateField.setText(releaseDate)
            })

            it.eventFlow.collectWhileStarted(this) { event ->
                when (event) {
                    EntryEditViewModel.Event.GoBackToList -> {
                        findNavController().navigateUp()
                    }
                    is EntryEditViewModel.Event.PopulateFields -> {
                        titleField.setText(event.title)
                        imageField.setText(event.imageUrl)
                        creator1Field.setText(event.creator1)
                        creator2Field.setText(event.creator2)
                    }
                    EntryEditViewModel.Event.GoToSearch -> {
                        val directions = EntryEditFragmentDirections.actionEntriesEditFragmentToSearchFragment(type)
                        findNavController().navigate(directions)
                    }
                }
            }
        }
    }

    private fun setFieldVisibilityAndHint(entryField: EntryField, hintTextRes: Int) {
        if (hintTextRes < 0) {
            entryField.visibility = View.GONE
        } else {
            entryField.setHint(hintTextRes)
            entryField.visibility = View.VISIBLE
        }
    }
}