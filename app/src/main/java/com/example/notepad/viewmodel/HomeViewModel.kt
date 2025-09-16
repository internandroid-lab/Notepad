package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val noteRepo: NoteRepository) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    private var currentSortType = SortType.BY_DATE

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            delay(500)
            val notes = when (currentSortType) {
                SortType.BY_DATE -> noteRepo.getAllNotes()
                SortType.BY_TITLE -> noteRepo.getAllNotesSortedByTitle()
            }
            _notes.value = notes
        }
    }

    fun toggleSelection(note: Note) {
        val current = _selectedNotes.value
        _selectedNotes.value =
            if (current.contains(note)) current - note else current + note
    }

    fun startSelection(note: Note) {
        _isSelectionMode.value = true
        _selectedNotes.value = setOf(note)
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedNotes.value = emptySet()
    }

    fun searchNotes(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadNotes()
        } else {
            viewModelScope.launch {
                val notes = noteRepo.searchNotes(query)
                _notes.value = notes
            }
        }
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
        if (!_isSearchMode.value) {
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
            noteRepo.insertNote(note)
            _event.emit("1 note imported")
            loadNotes()
        }
    }

    fun deleteSelectedNotes() {
        if (_selectedNotes.value.isNotEmpty()) {
            val size = _selectedNotes.value.size
            viewModelScope.launch {
                for (i in _selectedNotes.value) {
                    val finalNote = i.copy(onTrash = true)
                    noteRepo.updateNote(finalNote)
                }
                _event.emit("$size notes deleted")
                loadNotes()
            }
            clearSelection()
        }
    }

    fun selectAll() {
        if (_notes.value.toSet() != _selectedNotes.value) {
            _selectedNotes.value = _notes.value.toSet()
        } else {
            _selectedNotes.value = emptySet()
        }
    }
}