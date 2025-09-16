package com.example.notepad.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val noteId: Long = 0,
    val title: String = "",
    val content: String = "",
    val lastEdit: Date = Date(),
    val color: String = "#FFFFFF",
    val onTrash: Boolean = false
){
    val lastEditStr: String
        get() {
            val formatter = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())
            return "Last edit: ${formatter.format(lastEdit)}"
        }
}