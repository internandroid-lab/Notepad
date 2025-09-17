package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.CategoryRepository
import com.example.notepad.repository.CrossReferenceRepository
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoryNotesViewModel(
    private val cateRepo: CategoryRepository,
    private val noteRepo: NoteRepository,
    private val crossRefRepo: CrossReferenceRepository
) : ViewModel() {

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    private val _isSearchMode = MutableStateFlow(false)
    val isSearchMode: StateFlow<Boolean> = _isSearchMode.asStateFlow()

    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedNotes = MutableStateFlow<Set<Note>>(emptySet())
    val selectedNotes: StateFlow<Set<Note>> = _selectedNotes.asStateFlow()

    private val _event = MutableSharedFlow<String>()
    val event: SharedFlow<String> = _event.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")

    private val _currentSortType = MutableStateFlow(SortType.BY_DATE)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> =
        combine(_category.filterNotNull(),_searchQuery,_currentSortType){ category, query, sortType ->
            Triple(category, query, sortType)
        }.flatMapLatest { (category, query, sortType) ->
            if(query.isNotBlank()){
                crossRefRepo.searchNotesInCategory(category.categoryId, query)
            } else {
                when (sortType) {
                    SortType.BY_DATE -> crossRefRepo.getAllNotesInCategorySortedByDate(category.categoryId)
                    SortType.BY_TITLE -> crossRefRepo.getAllNotesInCategorySortedByTitle(category.categoryId)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), emptyList())

    fun loadCategory(categoryId: Long) {
        viewModelScope.launch {
            _category.value = cateRepo.getCategoryById(categoryId)
        }
    }

    fun searchNotes(query: String) {
        _searchQuery.value = query
    }

    fun sortNotes(sortType: SortType) {
        _currentSortType.value = sortType
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !_isSearchMode.value
        if (!_isSearchMode.value) {
            _searchQuery.value = ""
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

    fun deleteSelectedNotes() {
        if (_selectedNotes.value.isNotEmpty()) {
            viewModelScope.launch {
                for (i in _selectedNotes.value) {
                    val finalNote = i.copy(onTrash = true)
                    noteRepo.updateNote(finalNote)
                }
                _event.emit("${_selectedNotes.value.size} notes deleted")
                clearSelection()
            }
        }
    }

    fun selectAll() {
        if (_notes.value.toSet() != _selectedNotes.value) {
            _selectedNotes.value = _notes.value.toSet()
        } else {
            _selectedNotes.value = emptySet()
        }
    }

    fun importNote(note: Note) {
        viewModelScope.launch {
            val noteId = noteRepo.insertNote(note)
            crossRefRepo.addNoteToCategory(noteId, _category.value!!.categoryId)
            _event.emit("1 note imported")
        }
    }
}