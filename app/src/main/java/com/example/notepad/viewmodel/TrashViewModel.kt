package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashViewModel(private val noteRepo: NoteRepository): ViewModel() {
    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedNotes = MutableLiveData<Set<Note>>(emptySet())
    val selectedNotes: LiveData<Set<Note>> = _selectedNotes

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
            } catch (e: Exception) {
            }
        }
        loadTrashNotes()
    }

    fun toggleSelection(note: Note) {
        val current = _selectedNotes.value ?: emptySet()
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
        viewModelScope.launch {
            withContext(Dispatchers.IO){
                for(i in _selectedNotes.value){
                    noteRepo.deleteNote(i)
                }
            }
        }
        clearSelection()
        loadTrashNotes()
    }

    fun unDeleteSelectedNotes(){
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                for(i in _selectedNotes.value){
                    val finalNote = i.copy(onTrash = false)
                    noteRepo.updateNote(finalNote)
                }
            }
        }
        clearSelection()
        loadTrashNotes()
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