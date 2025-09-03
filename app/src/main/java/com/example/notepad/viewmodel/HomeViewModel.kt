package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isSearchMode = MutableLiveData<Boolean>(false)
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    enum class SortType {
        BY_DATE, BY_TITLE
    }

    private var currentSortType = SortType.BY_DATE

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            try {
                val notesList = when (currentSortType) {
                    SortType.BY_DATE -> repository.getNotesSortedByDate()
                    SortType.BY_TITLE -> repository.getNotesSortedByTitle()
                }
                _notes.value = notesList
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchNotes(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadNotes()
        } else {
            viewModelScope.launch {
                try {
                    val searchResults = repository.searchNotes(query)
                    _notes.value = searchResults
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !(_isSearchMode.value ?: false)
        if (!(_isSearchMode.value ?: false)) {
            _searchQuery.value = ""
            loadNotes()
        }
    }

    fun sortNotes(sortType: SortType) {
        currentSortType = sortType
        loadNotes()
    }
}