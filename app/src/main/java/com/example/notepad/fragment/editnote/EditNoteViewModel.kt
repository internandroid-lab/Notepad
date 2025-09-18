package com.example.notepad.fragment.editnote

import android.text.Editable
import android.text.Spannable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.implement.CategoryRepositoryImpl
import com.example.notepad.repository.implement.CrossReferenceRepositoryImpl
import com.example.notepad.repository.implement.NoteRepositoryImpl
import com.example.notepad.utils.TextStyle
import com.example.notepad.utils.toBase64
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Date

class EditNoteViewModel(
    private val noteRepo: NoteRepositoryImpl,
    private val cateRepo: CategoryRepositoryImpl,
    private val crossRefRepo: CrossReferenceRepositoryImpl
) : ViewModel() {

    private val _note = MutableStateFlow(Note())
    val note: StateFlow<Note> = _note.asStateFlow()

    private val _textStyle = MutableStateFlow(TextStyle())
    val textStyle: StateFlow<TextStyle> = _textStyle.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()


    fun loadNote(noteId: Long) {
        if (noteId > 0) {
            viewModelScope.launch {
                val note = noteRepo.getNoteById(noteId)
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

    fun loadCategory(): List<Category> = runBlocking {
        cateRepo.getAll()
    }

    fun getCategoriesOfNote(): List<Category> = runBlocking {
        crossRefRepo.getCategoriesOfNote(_note.value.noteId)
    }

    fun updateNote(wasChecked: Boolean, isNowChecked: Boolean, category: Category) {
        if (wasChecked && !isNowChecked) {
            viewModelScope.launch {
                crossRefRepo.deleteNoteFromCategory(_note.value.noteId, category.categoryId)
            }
        }
        if (!wasChecked && isNowChecked) {
            viewModelScope.launch {
                crossRefRepo.addNoteToCategory(_note.value.noteId, category.categoryId)
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

    fun updateColor(color: String?) {
        _note.value = _note.value.copy(color = color ?: "#FFFFFF")
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
            if (finalNote.noteId == 0L) {
                val noteId = noteRepo.insertNote(finalNote)
                if (categoryId > 0) {
                    crossRefRepo.addNoteToCategory(noteId, categoryId)
                }
            } else {
                noteRepo.updateNote(finalNote)
            }
            _event.emit("Note saved successfully")
        }
        return true
    }

    fun deleteNote() {
        val currentNote = _note.value
        val finalNote = currentNote.copy(onTrash = true)

        viewModelScope.launch {
            noteRepo.updateNote(finalNote)
        }
    }

    fun toggleBold() {
        _textStyle.value = _textStyle.value.copy(isBold = !_textStyle.value.isBold)
    }

    fun toggleItalic() {
        _textStyle.value = _textStyle.value.copy(isItalic = !_textStyle.value.isItalic)
    }

    fun toggleUnderline() {
        _textStyle.value = _textStyle.value.copy(isUnderline = !_textStyle.value.isUnderline)
    }

    fun toggleBackgroundTextColor(color: Int?) {
        _textStyle.value = _textStyle.value.copy(bgColor = color)
    }

    fun toggleTextColor(color: Int?) {
        _textStyle.value = _textStyle.value.copy(textColor = color)
    }

    fun toggleTextSize(size: Int) {
        _textStyle.value = _textStyle.value.copy(size = size)
    }
}