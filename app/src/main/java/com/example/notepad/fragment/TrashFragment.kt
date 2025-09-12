package com.example.notepad.fragment

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.databinding.FragmentTrashBinding
import com.example.notepad.db.entity.Note
import com.example.notepad.utils.AppUtil
import com.example.notepad.viewmodel.TrashViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.getValue

class TrashFragment : Fragment() {

    private var _binding: FragmentTrashBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: TrashViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentTrashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        AppUtil.setupKeyboardHiderForAllViews(view)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            showTrashDialog(note)
        }
        binding.recyclerViewNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
        }
    }

    private fun showTrashDialog(note: Note) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Note")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteNote(note)
                Toast.makeText(context, "Delete success", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Undelete") { _, _ ->
                val finalNote = note.copy(onTrash = false)
                viewModel.updateNote(finalNote)
                Toast.makeText(context, "Undelete success", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}