package com.example.notepad.repository

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Note
import com.example.notepad.db.dao.NoteDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NoteRepository(private val noteDao: NoteDao, private val crossRef: CrossReferenceDao) {

    suspend fun insertNote(note: Note): Long =
        withContext(Dispatchers.IO) { noteDao.insertNote(note) }

    suspend fun getAllNotes(): List<Note> = withContext(Dispatchers.IO) { noteDao.getAllNotes() }

    suspend fun getAllNotesSortedByTitle(): List<Note> =
        withContext(Dispatchers.IO) { noteDao.getAllNotesSortedByTitle() }

    suspend fun getNoteById(noteId: Long): Note? =
        withContext(Dispatchers.IO) { noteDao.getNoteById(noteId) }

    suspend fun getAllTrashedNotes(): List<Note> =
        withContext(Dispatchers.IO) { noteDao.getAllTrashedNotes() }

    suspend fun updateNote(note: Note) = withContext(Dispatchers.IO) { noteDao.updateNote(note) }

    suspend fun searchNotes(query: String): List<Note> =
        withContext(Dispatchers.IO) { noteDao.searchNotes(query) }

    suspend fun deleteNote(note: Note) =
        withContext(Dispatchers.IO) {
            crossRef.deleteNoteFromAllCategories(note.noteId)
            noteDao.deleteNote(note)
        }
}
