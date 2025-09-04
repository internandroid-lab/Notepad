package com.example.notepad.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notepad.R
import com.example.notepad.viewmodel.EditNoteViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class EditNoteFragment : Fragment() {

    private lateinit var backIcon: ImageView
    private lateinit var saveIcon: TextView
    private lateinit var undoIcon: TextView
    private lateinit var deleteIcon: TextView
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    private val viewModel: EditNoteViewModel by viewModel()

    private var isTitleEditing = false
    private var isContentEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_edit_note, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupObservers()
        setupClickListeners()

        val noteId = arguments?.getLong("noteId", 0L) ?: 0L
        viewModel.loadNote(noteId)
    }

    private fun initViews(view: View) {
        backIcon = view.findViewById(R.id.iv_back)
        saveIcon = view.findViewById(R.id.tv_save)
        undoIcon = view.findViewById(R.id.tv_undo)
        deleteIcon = view.findViewById(R.id.tv_delete)
        titleEditText = view.findViewById(R.id.et_title)
        contentEditText = view.findViewById(R.id.et_content)
    }

    private fun setupObservers() {
        viewModel.note.observe(viewLifecycleOwner) { note ->
            note?.let {
                if (!isTitleEditing && titleEditText.text.toString() != it.title) {
                    titleEditText.setText(it.title)
                    titleEditText.setSelection(it.title.length)
                }

                if (!isContentEditing && contentEditText.text.toString() != it.content) {
                    contentEditText.setText(it.content)
                    contentEditText.setSelection(it.content.length)
                }
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, context?.getString(R.string.add_new_successfully), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, context?.getString(R.string.add_new_failed), Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.deleteResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        backIcon.setOnClickListener {
            findNavController().navigateUp()
        }

        saveIcon.setOnClickListener {
            viewModel.saveNote()
        }

        undoIcon.setOnClickListener {
            viewModel.undoLastCharacter()
        }

        deleteIcon.setOnClickListener {
            viewModel.deleteNote()
        }

        titleEditText.setOnFocusChangeListener { _, hasFocus -> isTitleEditing = hasFocus }
        contentEditText.setOnFocusChangeListener { _, hasFocus -> isContentEditing = hasFocus }

        titleEditText.addTextChangedListener {
            viewModel.updateTitle(it.toString())
        }

        contentEditText.addTextChangedListener {
            viewModel.updateContent(it.toString())
        }
    }
}