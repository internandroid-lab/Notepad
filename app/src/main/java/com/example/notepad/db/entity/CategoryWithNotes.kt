package com.example.notepad.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CategoryWithNotes(
    @Embedded val category: Category,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryId"
    )
    val notes: List<Note>
)
