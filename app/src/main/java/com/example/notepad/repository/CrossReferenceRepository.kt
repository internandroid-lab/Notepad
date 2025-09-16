package com.example.notepad.repository

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CrossReferenceRepository(private val crossRef: CrossReferenceDao) {

    suspend fun addNoteToCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO){ crossRef.addNoteToCategory(CrossReference(noteId, categoryId)) }

    suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO){ crossRef.deleteNoteFromCategory(noteId, categoryId) }

    fun getAllNotesInCategorySortedByDate(categoryId: Long): Flow<List<Note>> =
        crossRef.getAllNotesInCategorySortedByDate(categoryId)

    fun getAllNotesInCategorySortedByTitle(categoryId: Long): Flow<List<Note>> =
        crossRef.getAllNotesInCategorySortedByTitle(categoryId)

    fun searchNotesInCategory(categoryId: Long, query: String): Flow<List<Note>> =
        crossRef.searchNotesInCategory(categoryId, query)

    suspend fun getCategoriesOfNote(noteId: Long): List<Category> =
        withContext(Dispatchers.IO){ crossRef.getCategoriesOfNote(noteId) }
}
