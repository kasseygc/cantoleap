package com.example.project.data.repository

import android.content.Context
import android.content.ContentValues
import com.example.project.data.local.DatabaseManager
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.models.VocabularyItem

class VocabularyRepository(context: Context) {

    private val databaseManager = DatabaseManager(context)

    fun getCategories(): List<VocabularyCategory> {
        return databaseManager.getCategories()
    }

    fun getVocabularyByCategory(category: String): List<VocabularyItem> {
        return databaseManager.getVocabularyByCategory(category)
    }

    fun getVocabularyById(id: String): VocabularyItem? {
        return databaseManager.getVocabularyById(id)
    }

    // Mark a vocabulary item as known or learning
    fun updateVocabularyStatus(userId: String, vocabularyId: String, status: Int) {
        val db = databaseManager.writableDatabase  // Use the property directly

        val values = ContentValues().apply {
            put(DatabaseManager.COLUMN_USER_ID, userId)
            put(DatabaseManager.COLUMN_VOCABULARY_ID, vocabularyId)
            put(DatabaseManager.COLUMN_STATUS, status)
            put(DatabaseManager.COLUMN_LAST_REVIEWED, System.currentTimeMillis())
        }

        // Try to update, if no row affected then insert
        val rowsAffected = db.update(
            DatabaseManager.TABLE_PROGRESS,
            values,
            "${DatabaseManager.COLUMN_USER_ID} = ? AND ${DatabaseManager.COLUMN_VOCABULARY_ID} = ?",
            arrayOf(userId, vocabularyId)
        )

        if (rowsAffected == 0) {
            db.insert(DatabaseManager.TABLE_PROGRESS, null, values)
        }
    }
}