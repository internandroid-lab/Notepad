package com.example.notepad.repository

import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Note
import com.example.notepad.db.dao.NoteDao

class NoteRepository(private val noteDao: NoteDao, private val crossRef: CrossReferenceDao) {

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun getAllNotes(): List<Note> = noteDao.getAllNotes()

    suspend fun getAllNotesSortedByTitle(): List<Note> = noteDao.getAllNotesSortedByTitle()

    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)

    suspend fun getAllTrashedNotes(): List<Note> = noteDao.getAllTrashedNotes()

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun searchNotes(query: String): List<Note> = noteDao.searchNotes(query)

    suspend fun deleteNote(note: Note) {
        crossRef.deleteNoteFromAllCategories(note.noteId)
        noteDao.deleteNote(note)
    }
}
