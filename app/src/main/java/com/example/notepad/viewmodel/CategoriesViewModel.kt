package com.example.notepad.viewmodel

import android.util.Log.e
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesViewModel(private val cateRepo: CategoryRepository) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    init {
        loadCategories()
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val category = Category(name = name.trim())
                cateRepo.insertCategory(category)
            _event.emit("Category added")
            loadCategories()
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            cateRepo.updateCategory(category)
            loadCategories()
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            cateRepo.deleteCategory(category)
            _event.emit("Category deleted")
            loadCategories()
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val categories = cateRepo.getAllCategories()
            _categories.value = categories
        }
    }
}