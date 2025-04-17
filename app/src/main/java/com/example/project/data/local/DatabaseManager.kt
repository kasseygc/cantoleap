package com.example.project.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.models.VocabularyItem

class DatabaseManager(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "cantoleap.db"
        const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_VOCABULARY = "vocabulary"

        // Column names for categories
        const val COLUMN_ID = "id"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_ICON_NAME = "icon_name"

        // Column names for vocabulary
        const val COLUMN_CATEGORY_ID = "category_id"
        const val COLUMN_CHINESE = "chinese"
        const val COLUMN_ROMANIZATION = "romanization"
        const val COLUMN_ENGLISH = "english"
        const val COLUMN_AUDIO_REF = "audio_ref"
        const val COLUMN_DIFFICULTY = "difficulty"

        // Progress table
        const val TABLE_PROGRESS = "progress"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_VOCABULARY_ID = "vocabulary_id"
        const val COLUMN_STATUS = "status"
        const val COLUMN_LAST_REVIEWED = "last_reviewed"
    }

    private val dbInitializer = DatabaseInitializer(context)

    init {
        // Make sure database is initialized
        dbInitializer.initializeDatabase()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Not needed as we're using a pre-populated database
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For future database upgrades
    }

    // Get all vocabulary categories
    fun getCategories(): List<VocabularyCategory> {
        val categories = mutableListOf<VocabularyCategory>()
        val db = readableDatabase

        val cursor = db.query(
            TABLE_CATEGORIES,
            null,
            null,
            null,
            null,
            null,
            COLUMN_NAME
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION))
                val iconName = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ICON_NAME))

                // Count words in this category
                val wordCount = getWordCountForCategory(id)

                categories.add(
                    VocabularyCategory(
                        id = id,
                        name = name,
                        description = description,
                        wordCount = wordCount,
                        progress = 0 // Will be updated with user progress later
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return categories
    }


    // Get word count for a category
    private fun getWordCountForCategory(categoryId: String): Int {
        val db = readableDatabase

        val query = "SELECT COUNT(*) FROM $TABLE_VOCABULARY WHERE $COLUMN_CATEGORY_ID = ?"
        val cursor = db.rawQuery(query, arrayOf(categoryId))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }

    // Get vocabulary items for a category
    fun getVocabularyByCategory(categoryId: String): List<VocabularyItem> {
        val items = mutableListOf<VocabularyItem>()
        val db = readableDatabase

        val selection = "$COLUMN_CATEGORY_ID = ?"
        val selectionArgs = arrayOf(categoryId)

        val cursor = db.query(
            TABLE_VOCABULARY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            COLUMN_DIFFICULTY
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID))
                val chinese = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE))
                val romanization = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROMANIZATION))
                val english = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH))
                val audioRef = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_REF))
                val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY))

                items.add(
                    VocabularyItem(
                        id = id,
                        category = categoryId,
                        chinese = chinese,
                        romanization = romanization,
                        english = english,
                        audioRef = audioRef,
                        difficulty = difficulty
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        return items
    }

    // Get a single vocabulary item by ID
    fun getVocabularyById(id: String): VocabularyItem? {
        val db = readableDatabase

        val selection = "$COLUMN_ID = ?"
        val selectionArgs = arrayOf(id)

        val cursor = db.query(
            TABLE_VOCABULARY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var item: VocabularyItem? = null

        if (cursor.moveToFirst()) {
            val categoryId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
            val chinese = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE))
            val romanization = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROMANIZATION))
            val english = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH))
            val audioRef = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AUDIO_REF))
            val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY))

            item = VocabularyItem(
                id = id,
                category = categoryId,
                chinese = chinese,
                romanization = romanization,
                english = english,
                audioRef = audioRef,
                difficulty = difficulty
            )
        }

        cursor.close()
        return item
    }
}