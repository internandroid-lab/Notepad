package com.example.notepad.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notepad.R
import com.example.notepad.adapter.CategoryAdapter
import com.example.notepad.databinding.FragmentCategoriesBinding
import com.example.notepad.db.entity.Category
import com.example.notepad.utils.AppUtil
import com.example.notepad.viewmodel.CategoriesViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CategoriesFragment : Fragment() {

    private var _binding: FragmentCategoriesBinding? = null
    private val binding get() = _binding!!
    private lateinit var categoryAdapter: CategoryAdapter

    private val viewModel: CategoriesViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        AppUtil.setupKeyboardHiderForAllViews(view)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            onCategoryClick = { category ->
                val bundle = bundleOf("categoryId" to category.categoryId)
                findNavController().navigate(
                    R.id.action_categoriesFragment_to_categoryNotesFragment,
                    bundle
                )
            },
            onEditClick = { category ->
                showEditCategoryDialog(category)
            },
            onDeleteClick = { category ->
                showDeleteCategoryDialog(category)
            }
        )
        binding.rvCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED){
                launch {
                    viewModel.categories.collect { categories ->
                        categoryAdapter.submitList(categories)
                    }
                }
                launch {
                    viewModel.event.collect { msg ->
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnAddCategory.setOnClickListener {
            val categoryName = binding.etCategoryName.text.toString()
            if (categoryName.isNotBlank()) {
                viewModel.addCategory(categoryName)
                binding.etCategoryName.text.clear()
            }
        }
    }

    private fun showEditCategoryDialog(category: Category) {
        val editText = EditText(requireContext()).apply {
            setText(category.name)
            selectAll()
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Category")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val newName = editText.text.toString().trim()
                if (newName.isNotBlank()) {
                    val updatedCategory = category.copy(name = newName)
                    viewModel.updateCategory(updatedCategory)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteCategoryDialog(category: Category) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Category")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteCategory(category)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}