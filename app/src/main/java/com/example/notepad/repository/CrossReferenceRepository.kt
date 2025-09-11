package com.example.notepad.repository

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note


class CrossReferenceRepository(private val crossRef: CrossReferenceDao) {

    suspend fun addNoteToCategory(noteId: Long, categoryId: Long) =
        crossRef.addNoteToCategory(CrossReference(noteId, categoryId))

    suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long) =
        crossRef.deleteNoteFromCategory(noteId, categoryId)

    suspend fun getAllNotesInCategory(categoryId: Long): List<Note> =
        crossRef.getAllNotesInCategory(categoryId)

    suspend fun getAllNotesInCategorySortedByTitle(categoryId: Long): List<Note> =
        crossRef.getAllNotesInCategorySortedByTitle(categoryId)

    suspend fun searchNotesInCategory(categoryId: Long, query: String): List<Note> =
        crossRef.searchNotesInCategory(categoryId, query)

    suspend fun getCategoriesOfNote(noteId: Long): List<Category> =
        crossRef.getCategoriesOfNote(noteId)
}
