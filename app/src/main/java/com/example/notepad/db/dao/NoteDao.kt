package com.example.notepad.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.notepad.db.entity.CategoryWithNotes
import com.example.notepad.db.entity.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("SELECT * FROM notes WHERE onTrash = 0 ORDER BY title ASC")
    suspend fun getNotesSortedByTitle(): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 0 ORDER BY lastEdit DESC")
    suspend fun getNotesSortedByDate(): List<Note>

    @Transaction
    @Query("SELECT * FROM categories WHERE categoryId = :id")
    suspend fun getCategoryWithNotes(id: Long): CategoryWithNotes?

    @Query("SELECT * FROM notes WHERE onTrash = 0")
    suspend fun getAllActiveNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 1")
    suspend fun getAllTrashedNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM notes WHERE onTrash = 0 AND title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastEdit DESC")
    suspend fun searchNotes(query: String): List<Note>

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: Long)

}