package com.example.notepad.repository

import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.entity.Category
//import com.example.notepad.db.entity.CategoryWithNotes
import com.example.notepad.db.entity.Note

//class CategoryRepository(private val categoryDao: CategoryDao) {
//    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)
//
//    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()
//
//    suspend fun getCategoryById(categoryId: Long): Category = categoryDao.getCategoryById(categoryId)
//
//    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)
//
//    suspend fun deleteCategory(category: Category) = categoryDao.deleteCategory(category)
//}

class CategoryRepository(private val categoryDao: CategoryDao, private val crossRef: CrossReferenceDao) {

    suspend fun insertCategory(category: Category): Long = categoryDao.insertCategory(category)

    suspend fun getAllCategories(): List<Category> = categoryDao.getAllCategories()

    suspend fun getCategoryById(categoryId: Long): Category? = categoryDao.getCategoryById(categoryId)

    suspend fun updateCategory(category: Category) = categoryDao.updateCategory(category)

    suspend fun deleteCategory(category: Category) {
        crossRef.deleteNoteRefsByCategory(category.categoryId)
        categoryDao.deleteCategory(category)
    }
}