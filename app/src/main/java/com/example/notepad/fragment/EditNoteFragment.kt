package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.StyleSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
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

        viewModel.textStyle.observe(viewLifecycleOwner) { textStyle ->
            binding.btnBold.isSelected = textStyle.isBold
            binding.btnItalic.isSelected = textStyle.isItalic
            binding.btnUnderline.isSelected = textStyle.isUnderline
            if (textStyle.bgColor != null) {
                binding.btnHighligh.background = textStyle.bgColor.toDrawable()
            } else {
                binding.btnHighligh.background = null
            }
            if (textStyle.textColor != null) {
                binding.btnTextColor.background = textStyle.textColor.toDrawable()
            } else {
                binding.btnTextColor.background = null
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

//        binding.etContent.addTextChangedListener {
//            viewModel.updateContent(it.toString())
//        }

        binding.etContent.addTextChangedListener(object : TextWatcher {
            private var startPos = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                startPos = start
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return
                if (binding.btnBold.isSelected) {
                    val end = startPos + 1
                    if (end <= s.length && startPos >= 0) {
                        s.setSpan(
                            StyleSpan(Typeface.BOLD),
                            startPos,
                            end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                viewModel.updateContent(s.toString())
            }
        })

        binding.btnBold.setOnClickListener {
            viewModel.updateBold()
        }

        binding.btnItalic.setOnClickListener {
            viewModel.updateItalic()
        }

        binding.btnUnderline.setOnClickListener {
            viewModel.updateUnderline()
        }

        binding.btnHighligh.setOnClickListener {
            openColorPicker{selectedColor ->
                viewModel.updateBackgroundColor(selectedColor?.toColorInt())
            }
        }

        binding.btnTextColor.setOnClickListener {
            openColorPicker{selectedColor ->
                viewModel.updateTextColor(selectedColor?.toColorInt())
            }
        }

        binding.btnSize.setOnClickListener {
            showTextSizeDialog{
                viewModel.updateTextSize(it)
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

    private fun showTextSizeDialog(onSizeSelected: (Int) -> Unit) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_size, null)
        val seekBar = dialogView.findViewById<SeekBar>(R.id.seekBar)
        val tvSelectedSize = dialogView.findViewById<TextView>(R.id.tvSelectedSize)
        val btnSetDefault = dialogView.findViewById<Button>(R.id.btnSetDefault)

        var selectedSize = 20
        seekBar.progress = selectedSize
        tvSelectedSize.text = "Selected: $selectedSize"

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                selectedSize = progress
                tvSelectedSize.text = "Selected: $selectedSize"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        btnSetDefault.setOnClickListener {
            selectedSize = 20
            seekBar.progress = 20
            tvSelectedSize.text = "Selected: $selectedSize"
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                onSizeSelected(selectedSize)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}