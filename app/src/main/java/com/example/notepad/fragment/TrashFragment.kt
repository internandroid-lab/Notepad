package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.MainActivity
import com.example.notepad.R
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.databinding.FragmentHomeBinding
import com.example.notepad.databinding.FragmentTrashBinding
import com.example.notepad.db.entity.Note
import com.example.notepad.utils.AppUtil
import com.example.notepad.utils.SortType
import com.example.notepad.viewmodel.HomeViewModel
import com.example.notepad.viewmodel.TrashViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date
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


        AppUtil.setupKeyboardHiderForAllViews(view)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            val bundle = bundleOf("noteId" to note.noteId)
            findNavController().navigate(R.id.action_homeFragment_to_editNoteFragment, bundle)
        }
        binding.recyclerViewNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

//    private fun setupObservers() {
//        viewModel.notes.observe(viewLifecycleOwner) { notes ->
//            noteAdapter.submitList(notes)
//        }
//    }



}