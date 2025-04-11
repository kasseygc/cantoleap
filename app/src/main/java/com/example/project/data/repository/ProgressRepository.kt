package com.example.project.data.repository

import android.content.Context
import com.example.project.data.local.VocabularyDbHelper
import com.google.firebase.auth.FirebaseAuth

class ProgressRepository(private val context: Context) {

    private val dbHelper = VocabularyDbHelper(context)
    private val auth = FirebaseAuth.getInstance()

    fun markWordAsKnown(vocabularyId: String) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        getVocabularyRepository().updateVocabularyStatus(userId, vocabularyId, 2)
    }

    fun markWordAsLearning(vocabularyId: String) {
        val userId = auth.currentUser?.uid ?: "anonymous"
        getVocabularyRepository().updateVocabularyStatus(userId, vocabularyId, 1)
    }

    fun resetProgress() {
        val userId = auth.currentUser?.uid ?: "anonymous"
        val db = dbHelper.writableDatabase

        db.delete(
            VocabularyDbHelper.TABLE_PROGRESS,
            "${VocabularyDbHelper.COLUMN_USER_ID} = ?",
            arrayOf(userId)
        )
    }

    private fun getVocabularyRepository(): VocabularyRepository {
        return VocabularyRepository(context)
    }
}