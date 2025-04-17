package com.example.project.data.repository

import android.util.Log
import com.example.project.data.models.Note
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ServerValue

class NotesRepository {

    private val database = FirebaseDatabase.getInstance().reference
    private val auth = FirebaseAuth.getInstance()

    fun addNote(vocabularyId: String, note: String, callback: (Boolean) -> Unit) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val userEmail = auth.currentUser?.email ?: "anonymous"

        val noteKey = database.child("notes").child(vocabularyId).push().key

        if (noteKey != null) {
            val noteData = mapOf(
                "userId" to userId,
                "userEmail" to userEmail,
                "note" to note,
                "timestamp" to ServerValue.TIMESTAMP,
                "upvotes" to 0,
                "downvotes" to 0
            )

            database.child("notes").child(vocabularyId).child(noteKey).setValue(noteData)
                .addOnCompleteListener { task ->
                    callback(task.isSuccessful)
                }
        } else {
            callback(false)
        }
    }

    fun getNotes(vocabularyId: String, callback: (List<Note>) -> Unit) {
        database.child("notes").child(vocabularyId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val notes = mutableListOf<Note>()

                    for (noteSnapshot in snapshot.children) {
                        val note = noteSnapshot.getValue(Note::class.java)
                        note?.id = noteSnapshot.key
                        note?.let { notes.add(it) }
                    }

                    // Sort by upvotes, most upvoted first
                    notes.sortByDescending { it.upvotes }

                    callback(notes)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotesRepository", "Error getting notes", error.toException())
                    callback(emptyList())
                }
            })
    }

    fun upvoteNote(vocabularyId: String, noteId: String) {
        val userId = auth.currentUser?.uid ?: return

        // First check if user has already voted
        database.child("user_votes").child(userId).child(noteId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // User hasn't voted on this note before
                        incrementVote(vocabularyId, noteId, "upvotes")
                        // Record the vote
                        database.child("user_votes").child(userId).child(noteId).setValue("up")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotesRepository", "Error checking votes", error.toException())
                }
            })
    }

    fun downvoteNote(vocabularyId: String, noteId: String) {
        val userId = auth.currentUser?.uid ?: return

        // First check if user has already voted
        database.child("user_votes").child(userId).child(noteId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        // User hasn't voted on this note before
                        incrementVote(vocabularyId, noteId, "downvotes")
                        // Record the vote
                        database.child("user_votes").child(userId).child(noteId).setValue("down")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("NotesRepository", "Error checking votes", error.toException())
                }
            })
    }

    private fun incrementVote(vocabularyId: String, noteId: String, voteType: String) {
        val noteRef = database.child("notes").child(vocabularyId).child(noteId).child(voteType)

        noteRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                var currentValue = mutableData.getValue(Int::class.java) ?: 0

                mutableData.value = currentValue + 1

                return Transaction.success(mutableData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, dataSnapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("NotesRepository", "Error incrementing vote", error.toException())
                }
            }
        })
    }
}