package com.example.notepad.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notepad.R
import com.example.notepad.db.Note
import java.text.SimpleDateFormat
import java.util.Locale

class NoteAdapter(private val onNoteClick: (Note) -> Unit) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.tv_note_title)
        private val lastEditTextView: TextView = itemView.findViewById(R.id.tv_last_edit)
        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy, hh:mm a", Locale.getDefault())

        fun bind(note: Note) {
            titleTextView.text = note.title
            lastEditTextView.text = "Last edit: ${dateFormatter.format(note.lastEdit)}"

            itemView.setOnClickListener {
                onNoteClick(note)
            }
        }
    }

    class NoteDiffCallback : DiffUtil.ItemCallback<Note>() {
        override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem.noteId == newItem.noteId
        }

        override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
            return oldItem == newItem
        }
    }
}