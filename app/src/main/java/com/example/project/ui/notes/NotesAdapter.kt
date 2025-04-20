package com.example.project.ui.notes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.models.Note

class NotesAdapter(
    private var notes: List<Note>,
    val onUpvote: (Note) -> Unit,
    val onDownvote: (Note) -> Unit
) : RecyclerView.Adapter<NotesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userTextView: TextView = itemView.findViewById(R.id.text_note_user)
        val noteTextView: TextView = itemView.findViewById(R.id.text_note_text)
        val upvoteButton: ImageView = itemView.findViewById(R.id.button_upvote)
        val upvoteCountTextView: TextView = itemView.findViewById(R.id.text_upvotes)
        val downvoteButton: ImageView = itemView.findViewById(R.id.button_downvote)
        val downvoteCountTextView: TextView = itemView.findViewById(R.id.text_downvotes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_community_notes, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val note = notes[position]
        holder.userTextView.text = note.userEmail
        holder.noteTextView.text = note.note
        holder.upvoteCountTextView.text = note.upvotes.toString()
        holder.downvoteCountTextView.text = note.downvotes.toString()

        holder.upvoteButton.setOnClickListener { onUpvote(note) }
        holder.downvoteButton.setOnClickListener { onDownvote(note) }
    }

    override fun getItemCount() = notes.size

    fun updateData(newNotes: List<Note>) {
        notes = newNotes
        notifyDataSetChanged()
    }
}