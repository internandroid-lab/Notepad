package com.example.notepad.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val noteRepo: NoteRepository) : ViewModel() {
    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes.asStateFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    private val _currentSortType = MutableStateFlow(SortType.BY_DATE)

    val notes: StateFlow<List<Note>> =
        combine(_searchQuery,_currentSortType){ query, sortType ->
            if(query.isNotBlank()){
                when(sortType){
                    SortType.BY_DATE -> noteRepo.getAllNotesSortedByDate()
                    SortType.BY_TITLE -> noteRepo.getAllNotesSortedByTitle()
                }
            } else {
                noteRepo.searchNotes(query)
            }
        }
        .flatMapLatest { it }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

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
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
        if (!_isSearchMode.value) {
            _searchQuery.value = ""
        }
    }

    fun sortNotes(sortType: SortType) {
        _currentSortType.value = sortType
    }

    fun importNote(note: Note) {
        viewModelScope.launch {
            noteRepo.insertNote(note)
            _event.emit("1 note imported")
        }
    }

    fun deleteSelectedNotes() {
        Log.d("MTHAI", "deleteSelectedNotes: 1")
        if (_selectedNotes.value.isNotEmpty()) {
            viewModelScope.launch {
                for (i in _selectedNotes.value) {
                    val finalNote = i.copy(onTrash = true)
                    noteRepo.updateNote(finalNote)
                }
                Log.d("MTHAI", "deleteSelectedNotes: 2")

                _event.emit("${_selectedNotes.value.size} notes deleted")
                clearSelection()
            }
        }
    }

    fun selectAll() {
        _selectedNotes.value =
            if (notes.value.toSet() != _selectedNotes.value) {
                notes.value.toSet()
            } else {
                emptySet()
            }
    }
}