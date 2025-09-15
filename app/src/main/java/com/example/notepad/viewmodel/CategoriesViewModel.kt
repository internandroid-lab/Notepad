package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesViewModel(private val cateRepo: CategoryRepository) : ViewModel() {

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event

    init {
        loadCategories()
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                val category = Category(name = name.trim())
                withContext(Dispatchers.IO){
                    cateRepo.insertCategory(category)
                }
                loadCategories()
                _event.emit("Category added")
            } catch (e: Exception) {
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    cateRepo.updateCategory(category)
                }
                loadCategories()
                _event.emit("Category updated name")
            } catch (e: Exception) {
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    cateRepo.deleteCategory(category)
                }
                loadCategories()
                _event.emit("Category deleted")
            } catch (e: Exception) {
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = withContext(Dispatchers.IO){
                    cateRepo.getAllCategories()
                }
                _categories.value = categories
            } catch (e: Exception) {
            }
        }
    }
}