package com.example.notepad.repository

import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.Note
import kotlinx.coroutines.flow.Flow

interface ICrossReferenceRepository {
    suspend fun addNoteToCategory(noteId: Long, categoryId: Long)

    suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long)

    fun getAllNotesInCategorySortedByDate(categoryId: Long): Flow<List<Note>>

    fun getAllNotesInCategorySortedByTitle(categoryId: Long): Flow<List<Note>>

    fun searchNotesInCategory(categoryId: Long, query: String): Flow<List<Note>>

    suspend fun getCategoriesOfNote(noteId: Long): List<Category>
}