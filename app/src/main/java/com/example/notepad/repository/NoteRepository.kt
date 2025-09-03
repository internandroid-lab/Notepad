package com.example.notepad.repository

import com.example.notepad.db.Note
import com.example.notepad.db.NoteDao

class NoteRepository(private val noteDao: NoteDao) {

    suspend fun getAllNotes(): List<Note> = noteDao.getAllNotes()

    suspend fun getNoteById(noteId: Long): Note? = noteDao.getNoteById(noteId)

    suspend fun insertNote(note: Note): Long = noteDao.insertNote(note)

    suspend fun updateNote(note: Note) = noteDao.updateNote(note)

    suspend fun deleteNote(noteId: Long) = noteDao.deleteNoteById(noteId)

    suspend fun searchNotes(query: String): List<Note> = noteDao.searchNotes(query)

    suspend fun getNotesSortedByTitle(): List<Note> = noteDao.getNotesSortedByTitle()

    suspend fun getNotesSortedByDate(): List<Note> = noteDao.getNotesSortedByDate()
}