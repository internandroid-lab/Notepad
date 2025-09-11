package com.example.notepad.viewmodel

import android.text.Editable
import android.text.Spannable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date

class EditNoteViewModel(
    private val noteRepo: NoteRepository,
    private val cateRepo: CategoryRepository,
    private val crossRefRepo: CrossReferenceRepository
) : ViewModel() {

    private val _note = MutableLiveData<Note>()
    val note: LiveData<Note?> = _note

    private val _textStyle = MutableLiveData<TextStyle>(TextStyle())
    val textStyle: LiveData<TextStyle> = _textStyle


    fun loadNote(noteId: Long, categoryId: Long? = null) {
        if (noteId > 0) {
            viewModelScope.launch {
                try {
                    val note = withContext(Dispatchers.IO){
                        noteRepo.getNoteById(noteId)
                    }
                    _note.value = note!!
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            _note.value = Note(
                title = "",
                content = "",
                lastEdit = Date()
            )
        }
        Log.d("HungDM", "EditNoteViewModel loadNote: categoryId: $categoryId")
    }

    suspend fun loadCategory(): List<Category> {
        return withContext(Dispatchers.IO){
            cateRepo.getAllCategories()
        }
    }

    suspend fun getCategoriesOfNote(): List<Category> {
        return withContext(Dispatchers.IO){
            crossRefRepo.getCategoriesOfNote(_note.value.noteId)
        }
    }

    fun updateNote(wasChecked: Boolean, isNowChecked: Boolean, category: Category){
        if (wasChecked && !isNowChecked) {
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    crossRefRepo.deleteNoteFromCategory(_note.value.noteId, category.categoryId)
                }
            }
        }
        if (!wasChecked && isNowChecked) {
            viewModelScope.launch {
                withContext(Dispatchers.IO){
                    crossRefRepo.addNoteToCategory(_note.value.noteId, category.categoryId)
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _note.value = _note.value?.copy(title = newTitle)
    }

    fun updateContent(spannable: Editable) {
        val encodedContent = (spannable as Spannable).toBase64()
        _note.value = _note.value?.copy(content = encodedContent)

    }

    fun saveNote(categoryId: Long): Boolean {
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
                        val noteId = noteRepo.insertNote(finalNote)
                        if(categoryId>0){
                            crossRefRepo.addNoteToCategory(noteId,categoryId)
                        }
                    } else {
                        noteRepo.updateNote(finalNote)
                    }
                }
            } catch (e: Exception) {
            }
        }
        return true
    }

    fun deleteNote(categoryId: Long){
        val currentNote = _note.value ?: return
        val finalNote = currentNote.copy(onTrash = true)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if(categoryId<=0) {
                        noteRepo.updateNote(finalNote)
                        Log.d("HungDM", "updateNote")
                    } else {
                        crossRefRepo.deleteNoteFromCategory(finalNote.noteId, categoryId)
                        Log.d("HungDM", "deleteNoteFromCategory: noteID: ${finalNote.noteId}, categoryID: $categoryId")
                    }
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
