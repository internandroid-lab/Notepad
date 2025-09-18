package com.example.notepad.repository.implement

import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
import com.example.notepad.repository.ICategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CategoryRepositoryImpl(
    private val categoryDao: CategoryDao,
    private val crossRef: CrossReferenceDao
) : ICategoryRepository {

    override suspend fun insertCategory(category: Category): Long =
        withContext(Dispatchers.IO) { categoryDao.insertCategory(category) }

    override fun getAllCategories(): Flow<List<Category>> = categoryDao.getAllCategories()

    override suspend fun getAll(): List<Category> =
        withContext(Dispatchers.IO) { categoryDao.getAll() }

    override suspend fun getCategoryById(categoryId: Long): Category? =
        withContext(Dispatchers.IO) { categoryDao.getCategoryById(categoryId) }

    override suspend fun updateCategory(category: Category) =
        withContext(Dispatchers.IO) { categoryDao.updateCategory(category) }

    override suspend fun deleteCategory(category: Category) =
        withContext(Dispatchers.IO) {
            crossRef.deleteNoteRefsByCategory(category.categoryId)
            categoryDao.deleteCategory(category)
        }
}