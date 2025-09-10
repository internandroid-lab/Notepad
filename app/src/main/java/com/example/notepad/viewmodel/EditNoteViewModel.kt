package com.example.notepad.viewmodel

import android.text.Editable
import android.text.Spannable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.toBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditNoteViewModel(private val repository: NoteRepository) : ViewModel() {

    private val _note = MutableLiveData<Note?>()
    val note: LiveData<Note?> = _note

    private val _textStyle = MutableLiveData<TextStyle>(TextStyle())
    val textStyle: LiveData<TextStyle> = _textStyle


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
                categoryId = if(categoryId==0L) null else categoryId,
                title = "",
                content = "",
                lastEdit = Date()
            )
        }
    }

    fun updateTitle(newTitle: String) {
        _note.value = _note.value?.copy(title = newTitle)
    }

    fun updateContent(spannable: Editable) {
        val encodedContent = (spannable as Spannable).toBase64()
        _note.value = _note.value?.copy(content = encodedContent)

    }

    fun saveNote(): Boolean {
        val currentNote = _note.value ?: return false
        val titleText = currentNote.title.trim()
        val contentText = currentNote.content.trim()
        Log.d("savenote","title: $titleText, content: $contentText")

        if (titleText.isEmpty() && contentText.isEmpty()) return false


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
            } catch (e: Exception) {
            }
        }
        return true
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
            } catch (e: Exception) {
            }
        }
    }

    fun updateBold() {
        _textStyle.value = _textStyle.value?.copy(isBold = !_textStyle.value!!.isBold)
    }

    fun updateItalic() {
        _textStyle.value = _textStyle.value?.copy(isItalic = !_textStyle.value!!.isItalic)
    }

    fun updateUnderline() {
        _textStyle.value = _textStyle.value?.copy(isUnderline = !_textStyle.value!!.isUnderline)
    }

    fun updateBackgroundColor(color: Int?) {
        _textStyle.value = _textStyle.value?.copy(bgColor = color)
    }

    fun updateTextColor(color: Int?) {
        _textStyle.value = _textStyle.value?.copy(textColor = color)
    }

    fun updateTextSize(size: Int) {
        _textStyle.value = _textStyle.value?.copy(size = size)
    }
}

data class TextStyle(
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false,
    val bgColor: Int? = null,
    val textColor: Int? = null,
    val size: Int = 20
)