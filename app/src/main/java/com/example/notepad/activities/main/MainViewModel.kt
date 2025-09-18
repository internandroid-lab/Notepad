package com.example.notepad.activities.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.implement.CategoryRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val cateRepo: CategoryRepositoryImpl) : ViewModel() {
    val categories: StateFlow<List<Category>> =
        cateRepo.getAllCategories()
            .stateIn(viewModelScope, SharingStarted.Companion.WhileSubscribed(1000, 0), emptyList())

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
    }

    fun exitSearchMode() {
        _isSearchMode.value = false
    }
}