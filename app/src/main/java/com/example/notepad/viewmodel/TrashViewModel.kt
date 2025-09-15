package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashViewModel(private val noteRepo: NoteRepository): ViewModel() {
    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event

    init {
        loadTrashNotes()
    }

    fun updateNote(note: Note){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    noteRepo.updateNote(note)
                }
            } catch (e: Exception) {
            }
        }
        loadTrashNotes()
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    noteRepo.deleteNote(note)
                }
                _event.emit("Note Deleted")
            } catch (e: Exception) {
            }
        }
        loadTrashNotes()
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

    fun deleteSelectedNotes(){
        if(_selectedNotes.value.isNotEmpty()){
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    for(i in _selectedNotes.value){
                        noteRepo.deleteNote(i)
                    }
                }
                _event.emit("${_selectedNotes.value.size} notes deleted")
            }
            clearSelection()
            loadTrashNotes()
        }
    }

    fun unDeleteSelectedNotes(){
        if(_selectedNotes.value.isNotEmpty()){
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    for(i in _selectedNotes.value){
                        val finalNote = i.copy(onTrash = false)
                        noteRepo.updateNote(finalNote)
                    }
                }
                _event.emit("${_selectedNotes.value.size} notes restored")
            }
            clearSelection()
            loadTrashNotes()
        }
    }

    fun selectAll(){
        if (_notes.value.toSet() != _selectedNotes.value){
            _selectedNotes.value = _notes.value.toSet()
        }else{
            _selectedNotes.value = emptySet()
        }
    }

    private fun loadTrashNotes() {
        viewModelScope.launch {
            delay(500)
            try {
                val notes = withContext(Dispatchers.IO){
                    noteRepo.getAllTrashedNotes()
                }
                _notes.value = notes
            } catch (e: Exception){
            }
        }
    }
}