package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.notepad.R
import com.example.notepad.databinding.FragmentEditNoteBinding
import com.example.notepad.utils.AppUtil
import com.example.notepad.viewmodel.EditNoteViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import yuku.ambilwarna.AmbilWarnaDialog
import androidx.core.graphics.toColorInt
import androidx.core.graphics.drawable.toDrawable

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditNoteViewModel by viewModel()

    private var isBold = false
    private var isItalic = false
    private var isUnderline = false
    private var textColor: Int? = null
    private var bgColor: Int? = null

    private var isTitleEditing = false
    private var isContentEditing = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val noteId = arguments?.getLong("noteId", 0L) ?: 0L
        val categoryId = arguments?.getLong("categoryId")
        if(noteId==0L){
            binding.tvDelete.visibility=View.GONE
            binding.tvExport.visibility=View.GONE
        }

        setupObservers()
        setupClickListeners()
        viewModel.loadNote(noteId, categoryId)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

//        AppUtil.setupKeyboardHiderForAllViews(view)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        activity?.findViewById<View>(R.id.toolbar)?.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        activity?.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
    }

    override fun onPause() {
        super.onPause()
        activity?.findViewById<View>(R.id.toolbar)?.visibility = View.GONE
    }

    private fun setupObservers() {
        viewModel.note.observe(viewLifecycleOwner) { note ->
            note?.let {
                if (!isTitleEditing && binding.etTitle.text.toString() != it.title) {
                    binding.etTitle.setText(it.title)
                    binding.etTitle.setSelection(it.title.length)
                }

                if (!isContentEditing && binding.etContent.text.toString() != it.content) {
                    binding.etContent.setText(it.content)
                    binding.etContent.setSelection(it.content.length)
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
                Toast.makeText(context, getString(R.string.delete_successfully), Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(context, getString(R.string.delete_failed), Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.tvSave.setOnClickListener {
            viewModel.saveNote()
        }

        binding.tvUndo.setOnClickListener {
            viewModel.undoLastCharacter()
        }

        binding.tvDelete.setOnClickListener {
            viewModel.deleteNote()
        }

        binding.tvExport.setOnClickListener {
            exportFolderLauncher.launch(null)
        }

        binding.etTitle.setOnFocusChangeListener { _, hasFocus -> isTitleEditing = hasFocus }
        binding.etContent.setOnFocusChangeListener { _, hasFocus -> isContentEditing = hasFocus }

        binding.etTitle.addTextChangedListener {
            viewModel.updateTitle(it.toString())
        }

        binding.etContent.addTextChangedListener {
            viewModel.updateContent(it.toString())
        }


        binding.btnBold.setOnClickListener {
            binding.btnBold.isSelected = !binding.btnBold.isSelected
        }

        binding.btnItalic.setOnClickListener {
            binding.btnItalic.isSelected = !binding.btnItalic.isSelected
        }

        binding.btnUnderline.setOnClickListener {
            binding.btnUnderline.isSelected = !binding.btnUnderline.isSelected
        }

        binding.btnHighligh.setOnClickListener {
            openColorPicker{selectedColor ->
                if (selectedColor != null) {
                    val colorInt = selectedColor.toColorInt()
                    binding.btnHighligh.background = colorInt.toDrawable()
                } else {
                    binding.btnHighligh.background = null
                }
            }
        }

        binding.btnTextColor.setOnClickListener {
            openColorPicker{selectedColor ->
                if (selectedColor != null) {
                    val colorInt = selectedColor.toColorInt()
                    binding.btnTextColor.background = colorInt.toDrawable()
                } else {
                    binding.btnTextColor.background = null
                }
            }
        }
    }

    private val exportFolderLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        if (uri != null) {
            val note = viewModel.note.value
            if (note != null) {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
                )

                AppUtil.exportNote(requireContext(), note, uri)
                Toast.makeText(requireContext(), "Exported: ${note.title}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "No folder selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openColorPicker(onColorSelected: (String?) -> Unit) {
        val defaultColor = "#FF0000".toColorInt()
        val colorPicker = AmbilWarnaDialog(
            requireContext(),
            defaultColor,
            true,
            object : AmbilWarnaDialog.OnAmbilWarnaListener {
                override fun onOk(dialog: AmbilWarnaDialog?, color: Int) {
                    val hexColor = String.format("#%08X", color)
                    onColorSelected(hexColor)
                }
                override fun onCancel(dialog: AmbilWarnaDialog?) {
                    onColorSelected(null)
                }
            })
        colorPicker.show()
    }
}