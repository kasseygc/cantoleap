package com.example.project.ui.demo

import android.content.Context
import android.util.Log
import com.example.project.data.repository.ProgressRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.android.gms.tasks.Task

class DemoManager(private val context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val progressRepository = ProgressRepository(context)

    fun resetUserProgress() {
        progressRepository.resetProgress()
    }

    fun switchDemoUser(userType: String, callback: (Boolean) -> Unit) {
        // Sign out current Firebase user
        auth.signOut()

        // Sign in with demo account
        when (userType) {
            "beginner" -> signInWithDemoAccount("demo_beginner@cantoleap.com", "demo123", callback)
            "intermediate" -> signInWithDemoAccount("demo_intermediate@cantoleap.com", "demo123", callback)
            "advanced" -> signInWithDemoAccount("demo_advanced@cantoleap.com", "demo123", callback)
            else -> callback(false)
        }
    }

    private fun signInWithDemoAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    callback(true)
                } else {
                    // Try to create the account if it doesn't exist
                    createDemoAccount(email, password, callback)
                }
            }
    }

    private fun createDemoAccount(email: String, password: String, callback: (Boolean) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task: Task<com.google.firebase.auth.AuthResult> ->
                if (task.isSuccessful) {
                    // Pre-populate some progress for demo accounts
                    prepopulateDemoData(email)
                    callback(true)
                } else {
                    Log.e("DemoManager", "Error creating demo account", task.exception)
                    callback(false)
                }
            }
    }

    private fun prepopulateDemoData(email: String) {
        // This would normally set different progress levels based on the account type
        // For simplicity, we'll do this manually in the UI for the demo
        Log.d("DemoManager", "Prepopulating data for $email")
    }

    fun resetCommunityContent() {
        // In a real app, you'd need admin privileges to reset Firebase data
        // For demo purposes, we can add some default notes

        val database = FirebaseDatabase.getInstance().reference

        // Clear existing notes and add demo notes
        database.child("notes").removeValue()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Add sample notes for demo
                    addDemoNotes()
                }
            }
    }

    private fun addDemoNotes() {
        val database = FirebaseDatabase.getInstance().reference

        // Sample notes for demo
        val demoNotes = mapOf(
            "greet_1" to listOf(
                mapOf(
                    "userId" to "demo_user_1",
                    "userEmail" to "demo1@example.com",
                    "note" to "I remember this as 'knee how' - sounds like 'how are your knees?'",
                    "timestamp" to System.currentTimeMillis(),
                    "upvotes" to 5,
                    "downvotes" to 1
                ),
                mapOf(
                    "userId" to "demo_user_2",
                    "userEmail" to "demo2@example.com",
                    "note" to "This is the standard greeting in most Cantonese-speaking regions.",
                    "timestamp" to System.currentTimeMillis(),
                    "upvotes" to 3,
                    "downvotes" to 0
                )
            ),
            "food_1" to listOf(
                mapOf(
                    "userId" to "demo_user_3",
                    "userEmail" to "demo3@example.com",
                    "note" to "Remember 'faan' rhymes with 'yawn' - you yawn when you're full of rice!",
                    "timestamp" to System.currentTimeMillis(),
                    "upvotes" to 4,
                    "downvotes" to 2
                )
            )
        )

        // Add demo notes to Firebase
        for ((vocabularyId, notes) in demoNotes) {
            for (note in notes) {
                database.child("notes").child(vocabularyId).push().setValue(note)
            }
        }
    }
}