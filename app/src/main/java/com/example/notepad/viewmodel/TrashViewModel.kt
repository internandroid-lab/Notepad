package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TrashViewModel(private val noteRepo: NoteRepository) : ViewModel() {
    val notes: StateFlow<List<Note>> =
        noteRepo.getAllTrashedNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000,0), emptyList())

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteRepo.updateNote(note)
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            noteRepo.deleteNote(note)
            _event.emit("Note Deleted")
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

    fun deleteSelectedNotes() {
        if (_selectedNotes.value.isNotEmpty()) {
            viewModelScope.launch {
                for (i in _selectedNotes.value) {
                    noteRepo.deleteNote(i)
                }
                _event.emit("${_selectedNotes.value.size} notes deleted")
                clearSelection()
            }
        }
    }

    fun unDeleteSelectedNotes() {
        if (_selectedNotes.value.isNotEmpty()) {
            viewModelScope.launch {
                for (i in _selectedNotes.value) {
                    val finalNote = i.copy(onTrash = false)
                    noteRepo.updateNote(finalNote)
                }
                _event.emit("${_selectedNotes.value.size} notes restored")
                clearSelection()
            }
        }
    }

    fun selectAll() {
        if (notes.value.toSet() != _selectedNotes.value) {
            _selectedNotes.value = notes.value.toSet()
        } else {
            _selectedNotes.value = emptySet()
        }
    }
}