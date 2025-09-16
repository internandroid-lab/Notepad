package com.example.notepad.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note

@Dao
interface CrossReferenceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNoteToCategory(crossRef: CrossReference)

    @Query("DELETE FROM note_category_cross_ref WHERE noteId = :noteId AND categoryId = :categoryId")
    suspend fun deleteNoteFromCategory(noteId: Long, categoryId: Long)

    @Query("DELETE FROM note_category_cross_ref WHERE noteId = :noteId")
    suspend fun deleteNoteFromAllCategories(noteId: Long)

    @Query("DELETE FROM note_category_cross_ref WHERE categoryId = :categoryId")
    suspend fun deleteNoteRefsByCategory(categoryId: Long)

    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_category_cross_ref AS nc 
            ON notes.noteId = nc.noteId
        WHERE nc.categoryId = :categoryId AND notes.onTrash = 0
        ORDER BY lastEdit DESC
    """)
    suspend fun getAllNotesInCategorySortedByDate(categoryId: Long): List<Note>

    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_category_cross_ref AS nc 
            ON notes.noteId = nc.noteId
        WHERE nc.categoryId = :categoryId AND notes.onTrash = 0
        ORDER BY notes.title ASC
    """)
    suspend fun getAllNotesInCategorySortedByTitle(categoryId: Long): List<Note>

    @Transaction
    @Query("""
        SELECT * FROM notes 
        INNER JOIN note_category_cross_ref AS nc 
            ON notes.noteId = nc.noteId
        WHERE nc.categoryId = :categoryId 
          AND notes.onTrash = 0 
          AND (notes.title LIKE '%' || :query || '%' OR notes.content LIKE '%' || :query || '%')
    """)
    suspend fun searchNotesInCategory(categoryId: Long, query: String): List<Note>

    @Transaction
    @Query("""
    SELECT * FROM categories 
    INNER JOIN note_category_cross_ref AS nc 
        ON categories.categoryId = nc.categoryId
    WHERE nc.noteId = :noteId
    """)
    suspend fun getCategoriesOfNote(noteId: Long): List<Category>

}
