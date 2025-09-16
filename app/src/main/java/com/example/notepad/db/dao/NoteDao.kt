package com.example.notepad.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.notepad.db.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {

    @Insert
    suspend fun insertNote(note: Note): Long

    @Query("SELECT * FROM notes WHERE onTrash = 0 ORDER BY lastEdit DESC")
    fun getAllNotesSortedByDate(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE onTrash = 0 ORDER BY title ASC")
    fun getAllNotesSortedByTitle(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE noteId = :noteId LIMIT 1")
    suspend fun getNoteById(noteId: Long): Note?

    @Query("SELECT * FROM notes WHERE onTrash = 1")
    fun getAllTrashedNotes(): Flow<List<Note>>

    @Update
    suspend fun updateNote(note: Note)

    @Query("""
        SELECT * FROM notes 
        WHERE onTrash = 0 
          AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%')
    """)
    fun searchNotes(query: String): Flow<List<Note>>

    @Delete
    suspend fun deleteNote(note: Note)

    @Query("UPDATE notes SET onTrash = 1 WHERE noteId = :noteId")
    suspend fun moveNoteToTrash(noteId: Long)

    @Query("UPDATE notes SET onTrash = 0 WHERE noteId = :noteId")
    suspend fun restoreNote(noteId: Long)
}
