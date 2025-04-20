package com.example.project.ui.notes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.models.Note
import com.example.project.data.repository.NotesRepository
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NotesBottomSheet : BottomSheetDialogFragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var editNewNote: EditText
    private lateinit var postButton: Button

    private lateinit var adapter: NotesAdapter
    private val notesRepository = NotesRepository()

    private var vocabularyId: String = ""

    companion object {
        const val ARG_VOCABULARY_ID = "vocabulary_id"

        fun newInstance(vocabularyId: String): NotesBottomSheet {
            val fragment = NotesBottomSheet()
            val args = Bundle()
            args.putString(ARG_VOCABULARY_ID, vocabularyId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            vocabularyId = it.getString(ARG_VOCABULARY_ID, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_notes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_notes)
        editNewNote = view.findViewById(R.id.edit_new_note)
        postButton = view.findViewById(R.id.button_post_note)

        setupRecyclerView()
        loadNotes()

        postButton.setOnClickListener {
            val noteText = editNewNote.text.toString().trim()
            if (noteText.isNotEmpty()) {
                addNote(noteText)
            } else {
                Toast.makeText(context, "Please enter a note", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NotesAdapter(
            emptyList(),
            onUpvote = { note ->
                note.id?.let { noteId ->
                    notesRepository.upvoteNote(vocabularyId, noteId)
                }
            },
            onDownvote = { note ->
                note.id?.let { noteId ->
                    notesRepository.downvoteNote(vocabularyId, noteId)
                }
            }
        )

        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter
    }

    private fun loadNotes() {
        notesRepository.getNotes(vocabularyId) { notes ->
            adapter.updateData(notes)
        }
    }

    private fun addNote(noteText: String) {
        notesRepository.addNote(vocabularyId, noteText) { success ->
            if (success) {
                editNewNote.text.clear()
                loadNotes() // Reload notes after adding
            } else {
                Toast.makeText(context, "Failed to add note", Toast.LENGTH_SHORT).show()
            }
        }
    }
}