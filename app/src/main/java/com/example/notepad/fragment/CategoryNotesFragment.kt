package com.example.notepad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.MainActivity
import com.example.notepad.R
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.databinding.FragmentCategoryNotesBinding
import com.example.notepad.utils.AppUtil
import com.example.notepad.utils.SortType
import com.example.notepad.viewmodel.CategoryNotesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoryNotesFragment : Fragment(), MainActivity.ToolbarController {

    private var _binding: FragmentCategoryNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAdapter: NoteAdapter

    private val viewModel: CategoryNotesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = arguments?.getLong("categoryId") ?: 0L
        viewModel.loadCategory(categoryId)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        (activity as? MainActivity)?.setToolbarController(this)
        AppUtil.setupKeyboardHiderForAllViews(view)
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadNotesByCategory()
        (activity as? MainActivity)?.setToolbarController(this)
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter { note ->
            val bundle = bundleOf("noteId" to note.noteId)
            findNavController().navigate(
                R.id.action_categoryNotesFragment_to_editNoteFragment,
                bundle
            )
        }
        binding.rvNotes.apply {
            adapter = noteAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            noteAdapter.submitList(notes)
        }

        viewModel.category.observe(viewLifecycleOwner) { category ->
            (activity as? MainActivity)?.updateToolbarTitle("Notepad Free\n${category.name}")
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            (activity as? MainActivity)?.showSearchField(isSearchMode)
        }
    }

    private fun setupClickListeners() {
        binding.fabAddNote.setOnClickListener {
            val categoryId = arguments?.getLong("categoryId") ?: 0L
            val bundle = bundleOf(
                "noteId" to 0L,
                "categoryId" to categoryId
            )
            findNavController().navigate(
                R.id.action_categoryNotesFragment_to_editNoteFragment,
                bundle
            )
        }
    }

    // ToolbarController implementation
    override fun onSearchClick() {
        viewModel.toggleSearchMode()
    }

    override fun onSortClick() {
        showSortDialog()
    }

    override fun onSearchTextChanged(query: String) {
        viewModel.searchNotes(query)
    }

    override fun updateTitle(title: String) {
        (activity as? MainActivity)?.updateToolbarTitle(title)
    }

    override fun showSearchField(show: Boolean) {
        (activity as? MainActivity)?.showSearchField(show)
    }

    private fun showSortDialog() {
        val sortOptions = arrayOf("Sort by Date", "Sort by Title")

        AlertDialog.Builder(requireContext())
            .setTitle("Sort Notes")
            .setItems(sortOptions) { _, which ->
                when (which) {
                    0 -> viewModel.sortNotes(SortType.BY_DATE)
                    1 -> viewModel.sortNotes(SortType.BY_TITLE)
                }
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Clear toolbar controller when fragment is destroyed
        (activity as? MainActivity)?.setToolbarController(null)
        _binding = null
    }
}