package com.example.notepad.adapter

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.notepad.databinding.ItemNoteBinding
import com.example.notepad.db.entity.Note
import androidx.core.graphics.toColorInt

class NoteAdapter(
    private val onNoteClick: (Note) -> Unit = {},
    private val onLongClick: (Note) -> Unit = {},
    private val isSelected: (Note) -> Boolean = {true}
) :
    ListAdapter<Note, NoteAdapter.NoteViewHolder>(NoteDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemNoteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int, payloads: List<Any?>) {
        if(payloads.isNotEmpty()){
            payloads.forEach { payload ->
                val bundle = payload as? Bundle ?: return@forEach

                if (bundle.containsKey("title")) {
                    holder.binding.tvNoteTitle.text = bundle.getString("title")
                }
                if (bundle.containsKey("lastEditStr")) {
                    holder.binding.tvLastEdit.text = bundle.getString("lastEditStr")
                }
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    inner class NoteViewHolder(val binding: ItemNoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note) {
            binding.tvNoteTitle.text = note.title
            binding.tvLastEdit.text = note.lastEditStr

            binding.root.setOnClickListener {
                onNoteClick(note)
            }

            if (isSelected(note)) {
                binding.groupLl.setBackgroundColor("#FFFACD".toColorInt())
            } else {
                binding.groupLl.setBackgroundColor(Color.WHITE)
            }

            binding.root.setOnLongClickListener {
                onLongClick(note)
                true
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

        override fun getChangePayload(oldItem: Note, newItem: Note): Any? {
            val bundle = Bundle()
            if(oldItem.title != newItem.title){
                bundle.putString("title", newItem.title)
            }
            if(oldItem.lastEditStr != newItem.lastEditStr){
                bundle.putString("lastEditStr", newItem.lastEditStr)
            }
            return if (bundle.size()==0) null else bundle
        }
    }
}