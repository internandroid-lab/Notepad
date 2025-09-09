package com.example.notepad.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isSearchMode = MutableLiveData<Boolean>(false)
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private val _searchQuery = MutableLiveData<String>("")

    private var currentSortType = SortType.BY_DATE

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            try {
                val notes = withContext(Dispatchers.IO) {
                    when (currentSortType) {
                        SortType.BY_DATE -> repository.getAllNotes()
                        SortType.BY_TITLE -> repository.getNotesSortedByTitle()
                    }
                }
                _notes.value = notes
            } catch (e: Exception) {
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
                    val notes = withContext(Dispatchers.IO) {
                        repository.searchNotes(query)
                    }
                    _notes.value = notes
                } catch (e: Exception) {
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

    fun importNote(note: Note) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.insertNote(note)
            }
        }
        Log.d("Import note",note.toString())
        loadNotes()
    }
}