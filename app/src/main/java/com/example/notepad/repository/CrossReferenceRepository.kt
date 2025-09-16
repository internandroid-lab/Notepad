package com.example.notepad.repository

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CrossReferenceRepository(private val crossRef: CrossReferenceDao) {

    suspend fun addNoteToCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO){ crossRef.addNoteToCategory(CrossReference(noteId, categoryId)) }

    suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO){ crossRef.deleteNoteFromCategory(noteId, categoryId) }

    suspend fun getAllNotesInCategory(categoryId: Long): List<Note> =
        withContext(Dispatchers.IO){ crossRef.getAllNotesInCategory(categoryId) }

    suspend fun getAllNotesInCategorySortedByTitle(categoryId: Long): List<Note> =
        withContext(Dispatchers.IO){ crossRef.getAllNotesInCategorySortedByTitle(categoryId) }

    suspend fun searchNotesInCategory(categoryId: Long, query: String): List<Note> =
        withContext(Dispatchers.IO){ crossRef.searchNotesInCategory(categoryId, query) }

    suspend fun getCategoriesOfNote(noteId: Long): List<Category> =
        withContext(Dispatchers.IO){ crossRef.getCategoriesOfNote(noteId) }
}
