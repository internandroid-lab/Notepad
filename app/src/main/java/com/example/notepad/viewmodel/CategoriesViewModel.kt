package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val cateRepo: CategoryRepository) : ViewModel() {

    val categories: StateFlow<List<Category>> =
        cateRepo.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    fun addCategory(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            val category = Category(name = name.trim())
            cateRepo.insertCategory(category)
            _event.emit("Category added")
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            cateRepo.updateCategory(category)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            cateRepo.deleteCategory(category)
            _event.emit("Category deleted")
        }
    }
}