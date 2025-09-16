package com.example.notepad.viewmodel

import android.text.Editable
import android.text.Spannable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.CategoryRepository
import com.example.notepad.repository.CrossReferenceRepository
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.TextStyle
import com.example.notepad.utils.toBase64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditNoteViewModel(
    private val noteRepo: NoteRepository,
    private val cateRepo: CategoryRepository,
    private val crossRefRepo: CrossReferenceRepository
) : ViewModel() {

    private val _note = MutableStateFlow(Note())
    val note: StateFlow<Note> = _note.asStateFlow()

    private val _textStyle = MutableStateFlow(TextStyle())
    val textStyle: StateFlow<TextStyle> = _textStyle.asStateFlow()


    fun loadNote(noteId: Long) {
        if (noteId > 0) {
            viewModelScope.launch {
                val note = withContext(Dispatchers.IO) {
                    noteRepo.getNoteById(noteId)
                }
                _note.value = note!!
            }
        } else {
            _note.value = Note(
                title = "",
                content = "",
                lastEdit = Date()
            )
        }
    }

    fun updateTextStyle(style: TextStyle) {
        _textStyle.value = style
    }

    suspend fun loadCategory(): List<Category> {
        return withContext(Dispatchers.IO) {
            cateRepo.getAllCategories()
        }
    }

    suspend fun getCategoriesOfNote(): List<Category> {
        return withContext(Dispatchers.IO) {
            crossRefRepo.getCategoriesOfNote(_note.value.noteId)
        }
    }

    fun updateNote(wasChecked: Boolean, isNowChecked: Boolean, category: Category) {
        if (wasChecked && !isNowChecked) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    crossRefRepo.deleteNoteFromCategory(_note.value.noteId, category.categoryId)
                }
            }
        }
        if (!wasChecked && isNowChecked) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    crossRefRepo.addNoteToCategory(_note.value.noteId, category.categoryId)
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _note.value = _note.value.copy(title = newTitle)
    }

    fun updateContent(spannable: Editable) {
        val encodedContent = (spannable as Spannable).toBase64()
        _note.value = _note.value.copy(content = encodedContent)

    }

    fun saveNote(categoryId: Long): Boolean {
        val currentNote = _note.value
        val titleText = currentNote.title.trim()
        val contentText = currentNote.content.trim()

        if (titleText.isEmpty() && contentText.isEmpty()) return false

        val finalNote = currentNote.copy(
            title = titleText.ifEmpty { "Untitled" },
            content = contentText,
            lastEdit = Date()
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (finalNote.noteId == 0L) {
                    val noteId = noteRepo.insertNote(finalNote)
                    if (categoryId > 0) {
                        crossRefRepo.addNoteToCategory(noteId, categoryId)
                    }
                } else {
                    noteRepo.updateNote(finalNote)
                }
            }
        }
        return true
    }

    fun deleteNote() {
        val currentNote = _note.value
        val finalNote = currentNote.copy(onTrash = true)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                noteRepo.updateNote(finalNote)
            }
        }
    }

    fun updateBold() {
        _textStyle.value = _textStyle.value.copy(isBold = !_textStyle.value.isBold)
    }

    fun updateItalic() {
        _textStyle.value = _textStyle.value.copy(isItalic = !_textStyle.value.isItalic)
    }

    fun updateUnderline() {
        _textStyle.value = _textStyle.value.copy(isUnderline = !_textStyle.value.isUnderline)
    }

    fun updateBackgroundColor(color: Int?) {
        _textStyle.value = _textStyle.value.copy(bgColor = color)
    }

    fun updateTextColor(color: Int?) {
        _textStyle.value = _textStyle.value.copy(textColor = color)
    }

    fun updateTextSize(size: Int) {
        _textStyle.value = _textStyle.value.copy(size = size)
    }
}
