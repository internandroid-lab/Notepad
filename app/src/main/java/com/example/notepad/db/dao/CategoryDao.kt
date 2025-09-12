package com.example.notepad.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.notepad.db.entity.Category
//import com.example.notepad.db.entity.CategoryWithNotes
import com.example.notepad.db.entity.Note

//@Dao
//interface CategoryDao {
//    @Insert(onConflict = OnConflictStrategy.REPLACE)
//    suspend fun insertCategory(category: Category): Long
//
//    @Query("SELECT * FROM categories")
//    suspend fun getAllCategories(): List<Category>
//
//    @Query("SELECT * FROM categories WHERE categoryId = :categoryId")
//    suspend fun getCategoryById(categoryId: Long): Category
//
//    @Update
//    suspend fun updateCategory(category: Category)
//
//    @Delete
//    suspend fun deleteCategory(category: Category)
//}

@Dao
interface CategoryDao {

    @Insert
    suspend fun insertCategory(category: Category): Long

    @Query("SELECT * FROM categories")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE categoryId = :categoryId LIMIT 1")
    suspend fun getCategoryById(categoryId: Long): Category?

    @Update
    suspend fun updateCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}
