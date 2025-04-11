package com.example.project.data.repository

import android.content.Context
import com.example.project.data.local.VocabularyDbHelper
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.models.VocabularyItem
import kotlin.collections.ArrayList

class VocabularyRepository(context: Context) {

    private val dbHelper = VocabularyDbHelper(context)

    init {
        // Make sure initial data is populated
        dbHelper.populateInitialData()
    }

    fun getCategories(): List<VocabularyCategory> {
        val db = dbHelper.readableDatabase
        val categories = ArrayList<VocabularyCategory>()

        val query = """
            SELECT category, COUNT(*) as word_count 
            FROM ${VocabularyDbHelper.TABLE_VOCABULARY} 
            GROUP BY category
        """.trimIndent()

        val cursor = db.rawQuery(query, null)

        if (cursor.moveToFirst()) {
            do {
                val categoryName = cursor.getString(cursor.getColumnIndexOrThrow("category"))
                val wordCount = cursor.getInt(cursor.getColumnIndexOrThrow("word_count"))

                // Get progress for this category based on user progress
                val progress = getCategoryProgress(categoryName)

                // Create a description based on category
                val description = when (categoryName) {
                    "Greetings" -> "Basic greetings and introductions"
                    "Family" -> "Family members and relationships"
                    "Numbers" -> "Numbers and counting"
                    "Food" -> "Common food items and restaurant phrases"
                    "Useful phrases" -> "Essential everyday expressions"
                    else -> "Cantonese vocabulary"
                }

                categories.add(
                    VocabularyCategory(
                        id = categoryName,
                        name = categoryName,
                        description = description,
                        wordCount = wordCount,
                        progress = progress
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return categories
    }

    fun getVocabularyByCategory(category: String): List<VocabularyItem> {
        val db = dbHelper.readableDatabase
        val items = ArrayList<VocabularyItem>()

        val selection = "${VocabularyDbHelper.COLUMN_CATEGORY} = ?"
        val selectionArgs = arrayOf(category)

        val cursor = db.query(
            VocabularyDbHelper.TABLE_VOCABULARY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(VocabularyDbHelper.COLUMN_ID))
                val chinese = cursor.getString(cursor.getColumnIndexOrThrow(VocabularyDbHelper.COLUMN_CHINESE))
                val romanization = cursor.getString(cursor.getColumnIndexOrThrow(VocabularyDbHelper.COLUMN_ROMANIZATION))
                val english = cursor.getString(cursor.getColumnIndexOrThrow(VocabularyDbHelper.COLUMN_ENGLISH))
                val audioRef = cursor.getString(cursor.getColumnIndexOrThrow(VocabularyDbHelper.COLUMN_AUDIO_REF))

                items.add(
                    VocabularyItem(
                        id = id,
                        category = category,
                        chinese = chinese,
                        romanization = romanization,
                        english = english,
                        audioRef = audioRef
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return items
    }

    // Get progress for a specific category (percentage)
    private fun getCategoryDescription(category: String): String {
        return when (category) {
            "Greetings" -> context.getString(R.string.category_greetings)
            "Family" -> context.getString(R.string.category_family)
            "Numbers" -> context.getString(R.string.category_numbers)
            "Food" -> context.getString(R.string.category_food)
            "Useful phrases" -> context.getString(R.string.category_useful_phrases)
            else -> context.getString(R.string.category_default)
        }
    }

    // Mark a vocabulary item as known or learning
    fun updateVocabularyStatus(userId: String, vocabularyId: String, status: Int) {
        val db = dbHelper.writableDatabase

        val values = android.content.ContentValues().apply {
            put(VocabularyDbHelper.COLUMN_USER_ID, userId)
            put(VocabularyDbHelper.COLUMN_VOCABULARY_ID, vocabularyId)
            put(VocabularyDbHelper.COLUMN_STATUS, status)
            put(VocabularyDbHelper.COLUMN_LAST_REVIEWED, System.currentTimeMillis())
        }

        // Try to update, if no row affected then insert
        val rowsAffected = db.update(
            VocabularyDbHelper.TABLE_PROGRESS,
            values,
            "${VocabularyDbHelper.COLUMN_USER_ID} = ? AND ${VocabularyDbHelper.COLUMN_VOCABULARY_ID} = ?",
            arrayOf(userId, vocabularyId)
        )

        if (rowsAffected == 0) {
            db.insert(VocabularyDbHelper.TABLE_PROGRESS, null, values)
        }
    }
}