package com.example.notepad.db

import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Converters {
    private val formatter = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())

    @TypeConverter
    fun toString(date: Date?): String? {
        return date?.let { formatter.format(it) }
    }

    @TypeConverter
    fun toDate(dateString: String?): Date? {
        return dateString?.let {
            formatter.parse(it)
        }
    }
}