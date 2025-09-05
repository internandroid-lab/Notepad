package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoriesViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = withContext(Dispatchers.IO){
                    repository.getAllCategories()
                }
                _categories.value = categories
            } catch (e: Exception) {
            }
        }
    }

    fun addCategory(name: String) {
        if (name.isBlank()) return

        viewModelScope.launch {
            try {
                val category = Category(name = name.trim())
                withContext(Dispatchers.IO){
                    repository.insertCategory(category)
                }
                loadCategories()
            } catch (e: Exception) {
            }
        }
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    repository.updateCategory(category)
                }
                loadCategories()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    repository.deleteCategory(category)
                }
                loadCategories()
            } catch (e: Exception) {
            }
        }
    }
}