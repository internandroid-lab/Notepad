package com.example.notepad.repository

import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CategoryRepository(
    private val categoryDao: CategoryDao,
    private val crossRef: CrossReferenceDao
) {

    suspend fun insertCategory(category: Category): Long =
        withContext(Dispatchers.IO) { categoryDao.insertCategory(category) }

    suspend fun getAllCategories(): List<Category> =
        withContext(Dispatchers.IO) { categoryDao.getAllCategories() }

    suspend fun getCategoryById(categoryId: Long): Category? =
        withContext(Dispatchers.IO) { categoryDao.getCategoryById(categoryId) }

    suspend fun updateCategory(category: Category) =
        withContext(Dispatchers.IO) { categoryDao.updateCategory(category) }

    suspend fun deleteCategory(category: Category) =
        withContext(Dispatchers.IO) {
            crossRef.deleteNoteRefsByCategory(category.categoryId)
            categoryDao.deleteCategory(category)
        }
}