package com.example.notepad.db.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class CategoryWithNotes(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "noteId",
        associateBy = Junction(CrossReference::class)
    )
    val notes: List<Note>
)