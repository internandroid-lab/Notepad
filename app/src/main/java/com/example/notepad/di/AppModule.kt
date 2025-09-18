package com.example.notepad.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.notepad.db.AppDatabase
import com.example.notepad.db.dao.CategoryDao
import com.example.notepad.db.dao.CrossReferenceDao
import com.example.notepad.db.dao.NoteDao
import com.example.notepad.repository.implement.CategoryRepositoryImpl
import com.example.notepad.repository.implement.CrossReferenceRepositoryImpl
import com.example.notepad.repository.implement.NoteRepositoryImpl
import com.example.notepad.fragment.categories.CategoriesViewModel
import com.example.notepad.fragment.categorynotes.CategoryNotesViewModel
import com.example.notepad.fragment.editnote.EditNoteViewModel
import com.example.notepad.fragment.home.HomeViewModel
import com.example.notepad.activities.main.MainViewModel
import com.example.notepad.fragment.trash.TrashViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module{
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "appdb"
        ).addCallback(object : RoomDatabase.Callback() {})
        .build()
    }

    single<NoteDao> { get<AppDatabase>().noteDao() }
    single<CategoryDao> { get<AppDatabase>().categoryDao() }
    single<CrossReferenceDao> { get<AppDatabase>().crossRefDao() }
    single<NoteRepositoryImpl> { NoteRepositoryImpl(get(),get()) }
    single<CategoryRepositoryImpl> { CategoryRepositoryImpl(get(),get()) }
    single<CrossReferenceRepositoryImpl> { CrossReferenceRepositoryImpl(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { HomeViewModel(get()) }
    viewModel { EditNoteViewModel(get(),get(),get()) }
    viewModel { CategoriesViewModel(get()) }
    viewModel { TrashViewModel(get()) }
    viewModel { CategoryNotesViewModel(get(),get(),get()) }
}