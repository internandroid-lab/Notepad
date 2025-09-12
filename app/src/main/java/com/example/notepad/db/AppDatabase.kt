package com.example.notepad.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.dao.NoteDao
import com.example.notepad.db.entity.Category
import com.example.notepad.db.entity.CrossReference
import com.example.notepad.db.entity.Note

@Database(
    entities = [Category::class, Note::class, CrossReference::class],
    version = 1,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    abstract fun categoryDao(): CategoryDao

    abstract fun crossRefDao(): CrossReferenceDao
}