package com.example.notepad.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.notepad.repository.NoteRepository

//class EditNoteViewModelFactory(private val repository: NoteRepository) : ViewModelProvider.Factory {
//    override fun <T : ViewModel> create(modelClass: Class<T>): T {
//        if (modelClass.isAssignableFrom(EditNoteViewModel::class.java)) {
//            @Suppress("UNCHECKED_CAST")
//            return EditNoteViewModel(repository) as T
//        }
//        throw IllegalArgumentException("Unknown ViewModel class")
//    }
//}