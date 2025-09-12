package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.MainActivity
import com.example.notepad.R
import com.example.notepad.adapter.NoteAdapter
import com.example.notepad.databinding.FragmentCategoryNotesBinding
import com.example.notepad.db.entity.Note
import com.example.notepad.utils.AppUtil
import com.example.notepad.utils.SortType
import com.example.notepad.viewmodel.CategoryNotesViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.Date

class CategoryNotesFragment : Fragment(), MainActivity.ToolbarController {

    private var _binding: FragmentCategoryNotesBinding? = null
    private val binding get() = _binding!!
    private lateinit var noteAdapter: NoteAdapter
    private var notesToExport: List<Note> = emptyList()

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

    override fun onDestroyView() {
        super.onDestroyView()
        (activity as? MainActivity)?.setToolbarController(null)
        _binding = null
    }

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

    override fun onAboutClick() {
        showAboutPopupMenu()
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter (
            onNoteClick = { note ->
                if(viewModel.isSelectionMode.value == true){
                    viewModel.toggleSelection(note)
                }else{
                    val categoryId = arguments?.getLong("categoryId")
                    val bundle = bundleOf(
                        "noteId" to note.noteId,
                        "categoryId" to categoryId
                    )
                    findNavController().navigate(
                        R.id.action_categoryNotesFragment_to_editNoteFragment,
                        bundle
                    )
                }
            },
            onLongClick = { note ->
                if (viewModel.isSelectionMode.value != true) {
                    viewModel.startSelection(note)
                } else {
                    viewModel.toggleSelection(note)
                }
            },
            isSelected = { note ->
                viewModel.selectedNotes.value?.contains(note) ?: false
            }
        )
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
            (activity as? MainActivity)?.updateToolbarTitle("Notepad\n${category.name}")
        }

        viewModel.isSearchMode.observe(viewLifecycleOwner) { isSearchMode ->
            (activity as? MainActivity)?.showSearchField(isSearchMode)
        }

        viewModel.selectedNotes.observe(viewLifecycleOwner) {
            noteAdapter.notifyDataSetChanged()
            binding.tvToolbarTitle.text =  it.size.toString()
        }

        viewModel.isSelectionMode.observe(viewLifecycleOwner) { isSelectionMode ->
            activity?.findViewById<View>(R.id.toolbar)?.visibility =
                if (isSelectionMode) View.GONE else View.VISIBLE
            binding.fabAddNote.visibility = if (isSelectionMode) View.GONE else View.VISIBLE
            binding.toolbar.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        }
    }

    private fun setupClickListeners() {
        binding.fabAddNote.setOnClickListener {
            val categoryId = arguments?.getLong("categoryId")
            val bundle = bundleOf(
                "noteId" to 0L,
                "categoryId" to categoryId
            )
            findNavController().navigate(
                R.id.action_categoryNotesFragment_to_editNoteFragment,
                bundle
            )
        }

        binding.ivBack.setOnClickListener {
            viewModel.clearSelection()
        }

        binding.tvDelete.setOnClickListener {
            Toast.makeText(requireContext(), "${viewModel.selectedNotes.value!!.size} notes deleted", Toast.LENGTH_SHORT).show()
            viewModel.deleteSelectedNotes()
        }

        binding.tvExport.setOnClickListener {
            notesToExport = viewModel.selectedNotes.value!!.toList()
            exportFolderLauncher.launch(null)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (viewModel.isSelectionMode.value == true) {
                viewModel.clearSelection()
            } else {
                isEnabled = false
                requireActivity().onBackPressed()
            }
        }
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

    private fun showAboutPopupMenu() {
        val popup = PopupMenu(requireContext(), requireActivity().findViewById(R.id.iv_about))
        popup.menuInflater.inflate(R.menu.popup_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_import -> {
                    openTextFilePicker()
                    true
                }
                R.id.action_export -> {
                    notesToExport = viewModel.notes.value!!
                    exportFolderLauncher.launch(null)
                    true
                }
                else -> false
            }
        }

        popup.show()
    }

    private fun openTextFilePicker() {
        val mimeTypes = arrayOf("text/plain")
        importFileLauncher.launch(mimeTypes)
    }

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val context = requireContext()

            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            val fileName = AppUtil.getFileName(context, uri) ?: "Imported Note"
            val fileContent = AppUtil.readTextFileFromUri(context, uri) ?: ""

            val note = Note(
                title = fileName,
                content = fileContent,
                lastEdit = Date()
            )
            viewModel.importNote(note)

            Toast.makeText(context, "Imported: $fileName", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "No file selected", Toast.LENGTH_SHORT).show()
        }
    }

    private val exportFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val context = requireContext()

            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            AppUtil.exportMultipleNotes(context, notesToExport, uri)
            Toast.makeText(
                context,
                "Exported: ${notesToExport.size} file",
                Toast.LENGTH_LONG
            ).show()
            notesToExport = emptyList()
        } else {
            Toast.makeText(requireContext(), "No folder selected", Toast.LENGTH_SHORT).show()
        }
    }
}