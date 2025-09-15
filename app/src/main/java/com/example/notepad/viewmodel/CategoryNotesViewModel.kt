package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.CategoryRepository
import com.example.notepad.repository.CrossReferenceRepository
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CategoryNotesViewModel(
    private val cateRepo: CategoryRepository,
    private val noteRepo: NoteRepository,
    private val crossRefRepo: CrossReferenceRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _category = MutableStateFlow(Category())
    val category: StateFlow<Category> = _category

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event

    private var currentSortType = SortType.BY_DATE

    fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            val category = withContext(Dispatchers.IO){
                cateRepo.getCategoryById(categoryId)
            }
            _category.value = category!!
            loadNotesByCategory()
        }
    }

    fun loadNotesByCategory() {
        viewModelScope.launch {
            delay(500)
            try {
                val notes= withContext(Dispatchers.IO){
                    when (currentSortType) {
                        SortType.BY_DATE -> crossRefRepo.getAllNotesInCategory(_category.value.categoryId)
                        SortType.BY_TITLE -> crossRefRepo.getAllNotesInCategorySortedByTitle(_category.value.categoryId)
                    }
                }
                _notes.value = notes
            } catch (e: Exception) {
            }
        }
    }

    fun searchNotes(query: String) {
        if (query.isEmpty()) {
            loadNotesByCategory()
        } else {
            viewModelScope.launch {
                try {
                    val searchResults = withContext(Dispatchers.IO){
                        crossRefRepo.searchNotesInCategory(_category.value.categoryId,query)
                    }
                    _notes.value = searchResults
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun sortNotes(sortType: SortType) {
        currentSortType = sortType
        loadNotesByCategory()
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
        if (!_isSearchMode.value ) {
            loadNotesByCategory()
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

    fun deleteSelectedNotes(){
        if(_selectedNotes.value.isNotEmpty()){
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    for(i in _selectedNotes.value){
                        val finalNote = i.copy(onTrash = true)
                        noteRepo.updateNote(finalNote)
                    }
                }
                _event.emit("${_selectedNotes.value.size} notes deleted")
            }
            clearSelection()
            loadNotesByCategory()
        }
    }

    fun selectAll(){
        if (_notes.value.toSet() != _selectedNotes.value){
            _selectedNotes.value = _notes.value.toSet()
        }else{
            _selectedNotes.value = emptySet()
        }
    }

    fun importNote(note: Note) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val noteId = noteRepo.insertNote(note)
                crossRefRepo.addNoteToCategory(noteId, _category.value.categoryId)
            }
            _event.emit("1 note imported")
        }
        loadNotesByCategory()
    }
}