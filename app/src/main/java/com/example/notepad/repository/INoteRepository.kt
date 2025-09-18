package com.example.notepad.repository

import com.example.notepad.db.entity.Note
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    suspend fun insertNote(note: Note): Long

    fun getAllNotesSortedByDate(): Flow<List<Note>>

    fun getAllNotesSortedByTitle(): Flow<List<Note>>

    suspend fun getNoteById(noteId: Long): Note?

    fun getAllTrashedNotes(): Flow<List<Note>>

    suspend fun updateNote(note: Note)

    fun searchNotes(query: String): Flow<List<Note>>

    suspend fun deleteNote(note: Note)
}