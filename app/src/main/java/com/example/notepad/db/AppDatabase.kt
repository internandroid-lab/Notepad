package com.example.notepad.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [Note::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "note_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateDatabase(database.noteDao())
                    }
                }
            }
        }

        private suspend fun populateDatabase(noteDao: NoteDao) {
            // Add sample notes
            val sampleNotes = listOf(
                Note(0, "Shopping List", "Milk\nBread\nEggs\nButter", Date()),
                Note(
                    0,
                    "Meeting Notes",
                    "Project deadline: Next Friday\nTeam meeting at 2 PM\nReview code changes",
                    Date()
                ),
                Note(
                    0,
                    "Ideas",
                    "New app features:\n- Dark mode\n- Cloud sync\n- Voice notes",
                    Date()
                ),
                Note(
                    0,
                    "To Do",
                    "1. Finish the app\n2. Test all features\n3. Submit for review",
                    Date()
                ),
                Note(
                    0,
                    "Quotes",
                    "\"The only way to do great work is to love what you do.\" - Steve Jobs",
                    Date()
                )
            )

            sampleNotes.forEach { note ->
                noteDao.insertNote(note)
            }
        }
    }
}