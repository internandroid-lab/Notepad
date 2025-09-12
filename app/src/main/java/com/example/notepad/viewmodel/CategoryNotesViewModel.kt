package com.example.notepad.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.CategoryRepository
import com.example.notepad.repository.CrossReferenceRepository
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryNotesViewModel(
    private val cateRepo: CategoryRepository,
    private val noteRepo: NoteRepository,
    private val crossRefRepo: CrossReferenceRepository
) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _category = MutableLiveData<Category>()
    val category: LiveData<Category> = _category

    private val _isSearchMode = MutableLiveData<Boolean>()
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private var currentSortType = SortType.BY_DATE

    fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = withContext(Dispatchers.IO){
                cateRepo.getCategoryById(categoryId)
            }
            _category.value = category!!
            loadNotesByCategory()
        }
        Log.d("HungDM", "CategoryNotesViewModel loadCategory: categoryId = $categoryId")
    }

    fun loadNotesByCategory() {
        viewModelScope.launch {
            try {
                val notes= withContext(Dispatchers.IO){
                    when (currentSortType) {
                        SortType.BY_DATE -> crossRefRepo.getAllNotesInCategory(_category.value.categoryId)
                        SortType.BY_TITLE -> crossRefRepo.getAllNotesInCategorySortedByTitle(_category.value.categoryId)
                    }
                }
                _notes.value = notes
            } catch (e: Exception) {
            }
        }
    }

    fun searchNotes(query: String) {
        if (query.isEmpty()) {
            loadNotesByCategory()
        } else {
            viewModelScope.launch {
                try {
                    val searchResults = withContext(Dispatchers.IO){
                        crossRefRepo.searchNotesInCategory(_category.value.categoryId,query)
                    }
                    _notes.value = searchResults
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sortNotes(sortType: SortType) {
        currentSortType = sortType
        loadNotesByCategory()
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !(_isSearchMode.value ?: false)
        if (!(_isSearchMode.value ?: false)) {
            loadNotesByCategory()
        }
    }

    fun importNote(note: Note) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val noteId = noteRepo.insertNote(note)
                crossRefRepo.addNoteToCategory(noteId, _category.value.categoryId)
            }
        }
        loadNotesByCategory()
    }
}