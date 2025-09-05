package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.core.os.bundleOf
import androidx.core.view.GravityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.R
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.utils.AppUtil
import com.example.notepad.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import androidx.core.view.isVisible
import com.example.notepad.databinding.FragmentHomeBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: HomeViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        AppUtil.setupKeyboardHiderForAllViews(view)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotes()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupObservers() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            if (isSearchMode) {
                binding.etSearch.visibility = View.VISIBLE
                binding.tvTitle.visibility = View.GONE
                binding.etSearch.requestFocus()
                binding.ivSearch.setImageResource(R.drawable.ic_close)
            } else {
                binding.etSearch.visibility = View.GONE
                binding.tvTitle.visibility = View.VISIBLE
                binding.etSearch.text.clear()
                binding.ivSearch.setImageResource(R.drawable.ic_search)
            }
        }
    }

    private fun setupClickListeners() {
        binding.fabAddNote.setOnClickListener {
            val bundle = bundleOf("noteId" to 0L)
            findNavController().navigate(R.id.action_homeFragment_to_editNoteFragment, bundle)
        }

        binding.ivSearch.setOnClickListener {
            viewModel.toggleSearchMode()
            if (binding.etSearch.isVisible) {
                binding.etSearch.requestFocus()
                val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(binding.etSearch, InputMethodManager.SHOW_IMPLICIT)
            }
        }

        binding.ivSort.setOnClickListener {
            showSortDialog()
        }

        binding.etSearch.addTextChangedListener { text ->
            viewModel.searchNotes(text.toString())
        }

        binding.ivMenu.setOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_notes -> {
                    Toast.makeText(context, "Notes clicked", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_categories -> {
                    Toast.makeText(context, "Categories clicked", Toast.LENGTH_SHORT).show()
                }


                R.id.nav_trash -> {
                    Toast.makeText(context, "Trash clicked", Toast.LENGTH_SHORT).show()
                }

                R.id.nav_settings -> {
                    Toast.makeText(context, "Settings clicked", Toast.LENGTH_SHORT).show()
                }
            }
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            true
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