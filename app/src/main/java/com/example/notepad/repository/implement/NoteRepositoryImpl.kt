package com.example.notepad.repository.implement

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.dao.NoteDao
import com.example.notepad.db.entity.Note
import com.example.notepad.repository.INoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class NoteRepositoryImpl(private val noteDao: NoteDao, private val crossRef: CrossReferenceDao) :
    INoteRepository {

    override suspend fun insertNote(note: Note): Long =
        withContext(Dispatchers.IO) { noteDao.insertNote(note) }

    override fun getAllNotesSortedByDate(): Flow<List<Note>> = noteDao.getAllNotesSortedByDate()

    override fun getAllNotesSortedByTitle(): Flow<List<Note>> = noteDao.getAllNotesSortedByTitle()

    override suspend fun getNoteById(noteId: Long): Note? =
        withContext(Dispatchers.IO) { noteDao.getNoteById(noteId) }

    override fun getAllTrashedNotes(): Flow<List<Note>> = noteDao.getAllTrashedNotes()

    override suspend fun updateNote(note: Note) =
        withContext(Dispatchers.IO) { noteDao.updateNote(note) }

    override fun searchNotes(query: String): Flow<List<Note>> = noteDao.searchNotes(query)

    override suspend fun deleteNote(note: Note) =
        withContext(Dispatchers.IO) {
            crossRef.deleteNoteFromAllCategories(note.noteId)
            noteDao.deleteNote(note)
        }
}