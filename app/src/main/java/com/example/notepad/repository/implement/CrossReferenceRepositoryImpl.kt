package com.example.notepad.repository.implement

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.ICrossReferenceRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CrossReferenceRepositoryImpl(private val crossRef: CrossReferenceDao) :
    ICrossReferenceRepository {

    override suspend fun addNoteToCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO) {
            crossRef.addNoteToCategory(
                CrossReference(
                    noteId,
                    categoryId
                )
            )
        }

    override suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long) =
        withContext(Dispatchers.IO) { crossRef.deleteNoteFromCategory(noteId, categoryId) }

    override fun getAllNotesInCategorySortedByDate(categoryId: Long): Flow<List<Note>> =
        crossRef.getAllNotesInCategorySortedByDate(categoryId)

    override fun getAllNotesInCategorySortedByTitle(categoryId: Long): Flow<List<Note>> =
        crossRef.getAllNotesInCategorySortedByTitle(categoryId)

    override fun searchNotesInCategory(categoryId: Long, query: String): Flow<List<Note>> =
        crossRef.searchNotesInCategory(categoryId, query)

    override suspend fun getCategoriesOfNote(noteId: Long): List<Category> =
        withContext(Dispatchers.IO) { crossRef.getCategoriesOfNote(noteId) }
}