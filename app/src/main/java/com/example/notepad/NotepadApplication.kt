package com.example.notepad

import android.app.Application
import com.example.notepad.db.AppDatabase

class NotepadApplication : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate() {
        super.onCreate()
    }
}