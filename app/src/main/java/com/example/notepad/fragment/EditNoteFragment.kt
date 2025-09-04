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
    private lateinit var titleEditText: EditText
    private lateinit var contentEditText: EditText

    private val viewModel: EditNoteViewModel by viewModel()

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

        // Load note if editing existing note
        val noteId = arguments?.getLong("noteId", 0L) ?: 0L
        viewModel.loadNote(noteId)
    }

    private fun initViews(view: View) {
        backIcon = view.findViewById(R.id.iv_back)
        saveIcon = view.findViewById(R.id.tv_save)
        undoIcon = view.findViewById(R.id.tv_undo)
        titleEditText = view.findViewById(R.id.et_title)
        contentEditText = view.findViewById(R.id.et_content)

        // Set hints for new note
        val noteId = arguments?.getLong("noteId", 0L) ?: 0L
        if (noteId == 0L) {
            titleEditText.hint = "Enter title..."
            contentEditText.hint = "Enter content..."
        }
    }

    private fun setupObservers() {
        viewModel.title.observe(viewLifecycleOwner) { title ->
            if (titleEditText.text.toString() != title) {
                titleEditText.setText(title)
                titleEditText.setSelection(title.length)
            }
        }

        viewModel.content.observe(viewLifecycleOwner) { content ->
            if (contentEditText.text.toString() != content) {
                contentEditText.setText(content)
                contentEditText.setSelection(content.length)
            }
        }

        viewModel.saveResult.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(context, "Note saved successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, "Please enter title or content", Toast.LENGTH_SHORT).show()
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

        titleEditText.addTextChangedListener { text ->
            viewModel.updateTitle(text.toString())
        }

        contentEditText.addTextChangedListener { text ->
            viewModel.updateContent(text.toString())
        }
    }
}