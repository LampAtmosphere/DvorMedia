package com.example.dvormedia

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class NotesAdapter(private val notesList: List<Note>, private val onNoteLongClick: (Note) -> Unit) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.date_text_view)
        val peopleTextView: TextView = itemView.findViewById(R.id.people_text_view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_note, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = notesList[position]
        holder.dateTextView.text = note.date
        holder.peopleTextView.text = note.people.joinToString("\n")

        holder.itemView.setOnLongClickListener {
            onNoteLongClick(note)
            true
        }
    }

    override fun getItemCount(): Int {
        return notesList.size
    }
}