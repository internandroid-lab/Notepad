package com.example.notepad.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.NoteRepository
import com.example.notepad.utils.SortType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(private val noteRepo: NoteRepository) : ViewModel() {

    private val _notes = MutableLiveData<List<Note>>()
    val notes: LiveData<List<Note>> = _notes

    private val _isSearchMode = MutableLiveData<Boolean>(false)
    val isSearchMode: LiveData<Boolean> = _isSearchMode

    private val _isSelectionMode = MutableLiveData(false)
    val isSelectionMode: LiveData<Boolean> = _isSelectionMode

    private val _selectedNotes = MutableLiveData<Set<Note>>(emptySet())
    val selectedNotes: LiveData<Set<Note>> = _selectedNotes

    private val _searchQuery = MutableLiveData<String>("")

    private var currentSortType = SortType.BY_DATE

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            delay(500)
            try {
                val notes = withContext(Dispatchers.IO) {
                    when (currentSortType) {
                        SortType.BY_DATE -> noteRepo.getAllNotes()
                        SortType.BY_TITLE -> noteRepo.getAllNotesSortedByTitle()
                    }
                }
                _notes.value = notes
            } catch (e: Exception) {
            }
        }
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

    fun searchNotes(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            loadNotes()
        } else {
            viewModelScope.launch {
                try {
                    val notes = withContext(Dispatchers.IO) {
                        noteRepo.searchNotes(query)
                    }
                    _notes.value = notes
                } catch (e: Exception) {
                }
            }
        }
    }

    fun toggleSearchMode() {
        _isSearchMode.value = !(_isSearchMode.value ?: false)
        if (!(_isSearchMode.value ?: false)) {
            _searchQuery.value = ""
            loadNotes()
        }
    }

    fun sortNotes(sortType: SortType) {
        currentSortType = sortType
        loadNotes()
    }

    fun addNote(note: Note) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                noteRepo.insertNote(note)
            }
        }
        loadNotes()
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
            }
            clearSelection()
            loadNotes()
        }
    }

    fun selectAll(){
        if (_notes.value.toSet() != _selectedNotes.value){
            _selectedNotes.value = _notes.value?.toSet()
        }else{
            _selectedNotes.value = emptySet()
        }
    }
}