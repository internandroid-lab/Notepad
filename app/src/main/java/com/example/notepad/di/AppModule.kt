package com.example.notepad.di

import androidx.room.Room
import com.example.notepad.db.AppDatabase
import com.example.notepad.db.MIGRATION
import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.dao.NoteDao
import com.example.notepad.repository.CategoryRepository
import com.example.notepad.repository.CrossReferenceRepository
import com.example.notepad.repository.NoteRepository
import com.example.notepad.viewmodel.CategoriesViewModel
import com.example.notepad.viewmodel.CategoryNotesViewModel
import com.example.notepad.viewmodel.EditNoteViewModel
import com.example.notepad.viewmodel.HomeViewModel
import com.example.notepad.viewmodel.MainViewModel
import com.example.notepad.viewmodel.TrashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "appdb"
        ).addMigrations(MIGRATION).build()
    }

    single<NoteDao> { get<AppDatabase>().noteDao() }
    single<CategoryDao> { get<AppDatabase>().categoryDao() }
    single<CrossReferenceDao> { get<AppDatabase>().crossRefDao() }
    single<NoteRepository> { NoteRepository(get(),get()) }
    single<CategoryRepository> { CategoryRepository(get(),get()) }
    single<CrossReferenceRepository> { CrossReferenceRepository(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { EditNoteViewModel(get(),get(),get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { TrashViewModel(get()) }
    viewModel { CategoryNotesViewModel(get(),get(),get()) }
}