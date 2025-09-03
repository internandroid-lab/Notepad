package com.example.notepad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.notepad.R
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.db.AppDatabase
import com.example.notepad.repository.NoteRepository
import com.example.notepad.viewmodel.HomeViewModel
import com.example.notepad.viewmodel.HomeViewModelFactory
import com.google.android.material.floatingactionbutton.FloatingActionButton

class HomeFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fab: FloatingActionButton
    private lateinit var searchIcon: ImageView
    private lateinit var sortIcon: ImageView
    private lateinit var searchEditText: EditText
    private lateinit var titleTextView: TextView
    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: HomeViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = NoteRepository(database.noteDao())
        HomeViewModelFactory(repository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotes()
    }

    private fun initViews(view: View) {
        recyclerView = view.findViewById(R.id.recycler_view_notes)
        fab = view.findViewById(R.id.fab_add_note)
        searchIcon = view.findViewById(R.id.iv_search)
        sortIcon = view.findViewById(R.id.iv_sort)
        searchEditText = view.findViewById(R.id.et_search)
        titleTextView = view.findViewById(R.id.tv_title)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            val bundle = bundleOf("noteId" to note.noteId)
            findNavController().navigate(R.id.action_homeFragment_to_editNoteFragment, bundle)
        }
        recyclerView.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            if (isSearchMode) {
                searchEditText.visibility = View.VISIBLE
                titleTextView.visibility = View.GONE
                searchEditText.requestFocus()
            } else {
                searchEditText.visibility = View.GONE
                titleTextView.visibility = View.VISIBLE
                searchEditText.text.clear()
            }
        }
    }

    private fun setupClickListeners() {
        fab.setOnClickListener {
            val bundle = bundleOf("noteId" to 0L)
            findNavController().navigate(R.id.action_homeFragment_to_editNoteFragment, bundle)
        }

        searchIcon.setOnClickListener {
            viewModel.toggleSearchMode()
        }

        sortIcon.setOnClickListener {
            showSortDialog()
        }

        searchEditText.addTextChangedListener { text ->
            viewModel.searchNotes(text.toString())
        }
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Sort by Date", "Sort by Title")

        AlertDialog.Builder(requireContext())
            .setTitle("Sort Notes")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> viewModel.sortNotes(HomeViewModel.SortType.BY_DATE)
                    1 -> viewModel.sortNotes(HomeViewModel.SortType.BY_TITLE)
                }
            }
            .show()
    }
}