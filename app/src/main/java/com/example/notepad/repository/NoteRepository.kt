package com.example.notepad.repository

import com.example.notepad.db.entity.Note
import com.example.notepad.db.dao.NoteDao

class NoteRepository(private val noteDao: NoteDao) {
    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun getAllActiveNotes(): List<Note> = noteDao.getAllActiveNotes()

    suspend fun getNotesSortedByTitle(): List<Note> = noteDao.getNotesSortedByTitle()

    suspend fun getNotesSortedByDate(): List<Note> = noteDao.getNotesSortedByDate()

    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun searchNotes(query: String): List<Note> = noteDao.searchNotes(query)

    suspend fun deleteNote(noteId: Long) = noteDao.deleteNoteById(noteId)

}