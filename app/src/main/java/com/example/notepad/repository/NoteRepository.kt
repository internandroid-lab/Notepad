package com.example.notepad.repository

import android.util.Log
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Note
import com.example.notepad.db.dao.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao, private val crossRef: CrossReferenceDao) {

    suspend fun insertNote(note: Note): Long =
        withContext(Dispatchers.IO) { noteDao.insertNote(note) }

    fun getAllNotesSortedByDate(): Flow<List<Note>> = noteDao.getAllNotesSortedByDate()

    fun getAllNotesSortedByTitle(): Flow<List<Note>> = noteDao.getAllNotesSortedByTitle()

    suspend fun getNoteById(noteId: Long): Note? =
        withContext(Dispatchers.IO) { noteDao.getNoteById(noteId) }

    fun getAllTrashedNotes(): Flow<List<Note>> = noteDao.getAllTrashedNotes()

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) { noteDao.updateNote(note) }

    fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    suspend fun deleteNote(note: Note) =
        withContext(Dispatchers.IO) {
            crossRef.deleteNoteFromAllCategories(note.noteId)
            noteDao.deleteNote(note)
        }
}
