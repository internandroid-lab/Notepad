package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.Note
import com.example.notepad.repository.NoteRepository
import kotlinx.coroutines.launch
import java.util.Date

class EditNoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _title = MutableLiveData<String>("")
    val title: LiveData<String> = _title

    private val _content = MutableLiveData<String>("")
    val content: LiveData<String> = _content

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private var isNewNote = true
    private var currentNoteId: Long = 0

    fun loadNote(noteId: Long) {
        if (noteId > 0) {
            isNewNote = false
            currentNoteId = noteId
            viewModelScope.launch {
                try {
                    val loadedNote = repository.getNoteById(noteId)
                    loadedNote?.let {
                        _note.value = it
                        _title.value = it.title
                        _content.value = it.content
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            // New note
            isNewNote = true
            _title.value = ""
            _content.value = ""
        }
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateContent(newContent: String) {
        _content.value = newContent
    }

    fun saveNote() {
        val titleText = _title.value?.trim() ?: ""
        val contentText = _content.value?.trim() ?: ""

        if (titleText.isEmpty() && contentText.isEmpty()) {
            _saveResult.value = false
            return
        }

        viewModelScope.launch {
            try {
                if (isNewNote) {
                    val newNote = Note(
                        noteId = 0,
                        title = titleText.ifEmpty { "Untitled" },
                        content = contentText,
                        lastEdit = Date()
                    )
                    repository.insertNote(newNote)
                } else {
                    val updatedNote = Note(
                        noteId = currentNoteId,
                        title = titleText.ifEmpty { "Untitled" },
                        content = contentText,
                        lastEdit = Date()
                    )
                    repository.updateNote(updatedNote)
                }
                _saveResult.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _saveResult.value = false
            }
        }
    }

    fun undoLastCharacter() {
        val currentContent = _content.value ?: ""
        if (currentContent.isNotEmpty()) {
            _content.value = currentContent.dropLast(1)
        }
    }
}