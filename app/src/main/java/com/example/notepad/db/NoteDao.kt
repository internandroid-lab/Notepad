package com.example.notepad.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("SELECT * FROM notes ORDER BY lastEdit DESC")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: Long)

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM notes WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastEdit DESC")
    suspend fun searchNotes(query: String): List<Note>

    @Query("SELECT * FROM notes ORDER BY title ASC")
    suspend fun getNotesSortedByTitle(): List<Note>

    @Query("SELECT * FROM notes ORDER BY lastEdit DESC")
    suspend fun getNotesSortedByDate(): List<Note>
}