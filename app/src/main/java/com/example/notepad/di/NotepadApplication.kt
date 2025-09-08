package com.example.notepad.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext
import org.koin.core.logger.Level

class NotepadApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        GlobalContext.startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@NotepadApplication)
            modules(appModule)
        }
    }
}