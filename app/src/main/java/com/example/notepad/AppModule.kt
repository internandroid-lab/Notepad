package com.example.notepad

import androidx.room.Room
import com.example.notepad.db.AppDatabase
import com.example.notepad.db.dao.NoteDao
import com.example.notepad.repository.NoteRepository
import com.example.notepad.viewmodel.EditNoteViewModel
import com.example.notepad.viewmodel.HomeViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "appdb"
        ).fallbackToDestructiveMigration().build()
    }

    single<NoteDao> { get<AppDatabase>().noteDao() }
    single<NoteRepository> { NoteRepository(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { EditNoteViewModel(get()) }
}