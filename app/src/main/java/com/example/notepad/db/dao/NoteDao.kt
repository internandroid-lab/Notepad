package com.example.notepad.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
//import com.example.notepad.db.entity.CategoryWithNotes
import com.example.notepad.db.entity.Note

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.Companion.REPLACE)
    suspend fun insertNote(note: Note): Long

    @Query("SELECT * FROM notes WHERE onTrash = 0")
    suspend fun getAllNotes(): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 0 AND categoryId = :categoryId")
    suspend fun getAllNotesInCategory(categoryId: Long): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 0 ORDER BY title ASC")
    suspend fun getNotesSortedByTitle(): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 0 AND categoryId = :categoryId ORDER BY title ASC")
    suspend fun getNotesInCategorySortedByTitle(categoryId: Long): List<Note>

    @Query("SELECT * FROM notes WHERE noteId = :noteId")
    suspend fun getNoteById(noteId: Long): Note?

    @Query("SELECT * FROM notes WHERE onTrash = 1")
    suspend fun getAllTrashedNotes(): List<Note>

    @Update
    suspend fun updateNote(note: Note)

    @Query("SELECT * FROM notes WHERE onTrash = 0 AND title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastEdit DESC")
    suspend fun searchNotes(query: String): List<Note>

    @Query("SELECT * FROM notes WHERE onTrash = 0 AND categoryId = :categoryId AND title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' ORDER BY lastEdit DESC")
    suspend fun searchNotesInCategory(query: String, categoryId: Long): List<Note>

    @Query("DELETE FROM notes WHERE noteId = :noteId")
    suspend fun deleteNoteById(noteId: Long)
}