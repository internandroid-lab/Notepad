package com.example.notepad.repository

import com.example.notepad.db.entity.Category
import kotlinx.coroutines.flow.Flow

interface ICategoryRepository {

    suspend fun insertCategory(category: Category): Long

    fun getAllCategories(): Flow<List<Category>>

    suspend fun getAll(): List<Category>

    suspend fun getCategoryById(categoryId: Long): Category?

    suspend fun updateCategory(category: Category)

    suspend fun deleteCategory(category: Category)
}