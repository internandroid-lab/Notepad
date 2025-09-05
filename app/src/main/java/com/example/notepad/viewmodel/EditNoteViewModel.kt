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
import java.util.Date

class EditNoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> = _saveResult

    private val _deleteResult = MutableLiveData<Boolean>()
    val deleteResult: LiveData<Boolean> = _deleteResult


    fun loadNote(noteId: Long, categoryId: Long? = null) {
        if (noteId > 0) {
            viewModelScope.launch {
                try {
                    val notes = withContext(Dispatchers.IO){
                        repository.getNoteById(noteId)
                    }
                    _note.value = notes
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            _note.value = Note(
                categoryId = categoryId,
                title = "",
                content = "",
                lastEdit = Date()
            )
        }
    }

    fun updateTitle(newTitle: String) {
        _note.value = _note.value?.copy(title = newTitle)
    }

    fun updateContent(newContent: String) {
        _note.value = _note.value?.copy(content = newContent)
    }

    fun saveNote() {
        val currentNote = _note.value ?: return
        val titleText = currentNote.title.trim()
        val contentText = currentNote.content.trim()

        if (titleText.isEmpty() && contentText.isEmpty()) {
            _saveResult.value = false
            return
        }

        val finalNote = currentNote.copy(
            title = titleText.ifEmpty { "Untitled" },
            content = contentText,
            lastEdit = Date()
        )

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (finalNote.noteId == 0L) {
                        repository.insertNote(finalNote)
                    } else {
                        repository.updateNote(finalNote)
                    }
                }
                _saveResult.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _saveResult.value = false
            }
        }
    }

    fun undoLastCharacter() {
        val currentContent = _note.value?.content ?: return
        if (currentContent.isNotEmpty()) {
            _note.value = _note.value?.copy(content = currentContent.dropLast(1))
        }
    }

    fun deleteNote(){
        val currentNote = _note.value ?: return
        val finalNote = currentNote.copy(onTrash = true)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateNote(finalNote)
                }
                _deleteResult.value = true
            } catch (e: Exception) {
                _deleteResult.value = false
            }
        }
    }
}