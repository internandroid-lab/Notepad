package com.example.notepad.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index


@Entity(
    tableName = "note_category_cross_ref",
    primaryKeys = ["noteId", "categoryId"],
    foreignKeys = [
        ForeignKey(
            entity = Note::class,
            parentColumns = ["noteId"],
            childColumns = ["noteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("noteId"), Index("categoryId")]
)
data class CrossReference(
    val noteId: Long,
    val categoryId: Long
)

