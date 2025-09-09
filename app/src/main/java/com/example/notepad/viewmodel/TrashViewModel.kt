package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrashViewModel(private val repository: NoteRepository): ViewModel() {
    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    init {
        loadTrashNotes()
    }

    fun loadTrashNotes() {
        viewModelScope.launch {
            try {
                val notes = withContext(Dispatchers.IO){
                    repository.getAllTrashedNotes()
                }
                _notes.value = notes
            } catch (e: Exception){
            }
        }
    }

    fun updateNote(note: Note){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateNote(note)
                    loadTrashNotes()
                }
            } catch (e: Exception) {
            }
        }
    }

    fun deleteNote(note: Note){
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO){
                    repository.deleteNote(note.noteId)
                    loadTrashNotes()
                }
            } catch (e: Exception) {
            }
        }
    }
}