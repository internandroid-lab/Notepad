package com.example.notepad.fragment

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.text.style.UnderlineSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.PopupMenu
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
import androidx.lifecycle.lifecycleScope
import com.example.notepad.utils.toSpannable
import kotlinx.coroutines.launch

class EditNoteFragment : Fragment() {

    private var _binding: FragmentEditNoteBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditNoteViewModel by viewModel()

    private var categoryId: Long = -1L

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
        val cateId = arguments?.getLong("categoryId") ?: -1L
        categoryId = cateId
        if(noteId==0L){
            binding.ivAbout.visibility=View.GONE
        }

        setupObservers()
        setupClickListeners()
        viewModel.loadNote(noteId, categoryId)

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            findNavController().navigateUp()
        }

        AppUtil.setupKeyboardHiderForAllViews(view)

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
                    val safePos = it.title.length.coerceAtMost(binding.etTitle.text?.length ?: 0)
                    binding.etTitle.setSelection(safePos)
                }

                if (!isContentEditing) {
                    val spanned = it.content.toSpannable()
                    if (binding.etContent.text.toString() != spanned.toString()) {
                        binding.etContent.setText(spanned)
                        val safePos = spanned.length.coerceAtMost(binding.etContent.text?.length ?: 0)
                        binding.etContent.setSelection(safePos)
                    }
                }
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
            val success = viewModel.saveNote(categoryId)
            if(success){
                Toast.makeText(context, context?.getString(R.string.save_successfully), Toast.LENGTH_SHORT).show()
                if(viewModel.note.value?.noteId==0L) findNavController().navigateUp()
            } else {
                Toast.makeText(context, context?.getString(R.string.save_failed), Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivAbout.setOnClickListener {
            showEditMenu()
        }

        binding.etTitle.setOnFocusChangeListener { _, hasFocus -> isTitleEditing = hasFocus }
        binding.etContent.setOnFocusChangeListener { _, hasFocus -> isContentEditing = hasFocus }

        binding.etTitle.addTextChangedListener {
            viewModel.updateTitle(it.toString())
        }

        binding.etContent.addTextChangedListener(object : TextWatcher {
            private var startPos = 0

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                startPos = start
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s == null) return

                var start = startPos
                var end = startPos + 1

                if (start < 0) start = 0
                if (end > s.length) end = s.length

                if (start < end) {
                    if (binding.btnBold.isSelected) {
                        s.setSpan(
                            StyleSpan(Typeface.BOLD),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    if (binding.btnItalic.isSelected) {
                        s.setSpan(
                            StyleSpan(Typeface.ITALIC),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    if (binding.btnUnderline.isSelected) {
                        s.setSpan(
                            UnderlineSpan(),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    viewModel.textStyle.value?.textColor?.let { color ->
                        s.setSpan(
                            ForegroundColorSpan(color),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    viewModel.textStyle.value?.bgColor?.let { color ->
                        s.setSpan(
                            BackgroundColorSpan(color),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }

                    viewModel.textStyle.value?.size?.let { size ->
                        s.setSpan(
                            AbsoluteSizeSpan(size, true),
                            start, end,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                    }
                }
                viewModel.updateContent(s)
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

    private fun showEditMenu() {
        val popup = PopupMenu(requireContext(), requireActivity().findViewById(R.id.iv_about))
        popup.menuInflater.inflate(R.menu.edit_menu, popup.menu)

        val addToCategoryItem = popup.menu.findItem(R.id.add_to_category)

        addToCategoryItem.isVisible = categoryId <= 0

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_delete -> {
                    viewModel.deleteNote(categoryId)
                    findNavController().navigateUp()
                    true
                }
                R.id.add_to_category -> {
                    showCategoryDialog()
                    true
                }
                R.id.action_export -> {
                    exportFolderLauncher.launch(null)
                    true
                }
                else -> false
            }
        }
        popup.show()
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

        var selectedSize = viewModel.textStyle.value!!.size
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

    private fun showCategoryDialog() {
        lifecycleScope.launch {
            val categories = viewModel.loadCategory()
            val categoryOfNote = viewModel.getCategoriesOfNote()
            val categoryNames = categories.map { it.name }.toTypedArray()

            val initialCheckedItems = BooleanArray(categories.size) { index ->
                categoryOfNote.any { it.categoryId == categories[index].categoryId }
            }

            val checkedItems = initialCheckedItems.copyOf()

            AlertDialog.Builder(requireContext())
                .setTitle("Select category")
                .setMultiChoiceItems(categoryNames, checkedItems) { _, which, isChecked ->
                    checkedItems[which] = isChecked
                }
                .setPositiveButton("OK") { _, _ ->
                    categories.forEachIndexed { index, category ->
                        val wasChecked = initialCheckedItems[index]
                        val isNowChecked = checkedItems[index]
                        viewModel.updateNote(wasChecked, isNowChecked, category)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

}