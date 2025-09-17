package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.CategoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn

class MainViewModel(private val cateRepo: CategoryRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> =
        cateRepo.getAllCategories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000,0), emptyList())

}