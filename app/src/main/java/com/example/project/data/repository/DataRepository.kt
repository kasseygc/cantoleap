package com.example.project.data.repository

import android.content.ContentValues
import android.content.Context
import com.example.project.data.local.DatabaseManager
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.models.VocabularyItem

class DataRepository(context: Context) {
    private val dbHelper = DatabaseManager(context)

    fun getCategories(): List<VocabularyCategory> {
        val categories = mutableListOf<VocabularyCategory>()
        val db = dbHelper.readableDatabase

        val cursor = db.query(
            DatabaseManager.TABLE_CATEGORIES,
            null,
            null,
            null,
            null,
            null,
            DatabaseManager.COLUMN_NAME
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ID))
                val name = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_NAME))
                val description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_DESCRIPTION))
                val iconName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ICON_NAME))

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
        db.close()
        return categories
    }

    private fun getWordCountForCategory(categoryId: String): Int {
        val db = dbHelper.readableDatabase
        val query = "SELECT COUNT(*) FROM ${DatabaseManager.TABLE_VOCABULARY} WHERE ${DatabaseManager.COLUMN_CATEGORY_ID} = ?"
        val cursor = db.rawQuery(query, arrayOf(categoryId))

        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0)
        }

        cursor.close()
        return count
    }

    fun getVocabularyByCategory(categoryId: String): List<VocabularyItem> {
        val items = mutableListOf<VocabularyItem>()
        val db = dbHelper.readableDatabase

        val selection = "${DatabaseManager.COLUMN_CATEGORY_ID} = ?"
        val selectionArgs = arrayOf(categoryId)

        val cursor = db.query(
            DatabaseManager.TABLE_VOCABULARY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            DatabaseManager.COLUMN_DIFFICULTY
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ID))
                val chinese = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_CHINESE))
                val romanization = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ROMANIZATION))
                val english = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ENGLISH))
                val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_DIFFICULTY))

                items.add(
                    VocabularyItem(
                        id = id,
                        category = categoryId,
                        chinese = chinese,
                        romanization = romanization,
                        english = english,
                        difficulty = difficulty
                    )
                )
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return items
    }

    fun getVocabularyById(id: String): VocabularyItem? {
        val db = dbHelper.readableDatabase
        val selection = "${DatabaseManager.COLUMN_ID} = ?"
        val selectionArgs = arrayOf(id)

        val cursor = db.query(
            DatabaseManager.TABLE_VOCABULARY,
            null,
            selection,
            selectionArgs,
            null,
            null,
            null
        )

        var item: VocabularyItem? = null

        if (cursor.moveToFirst()) {
            val categoryId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_CATEGORY_ID))
            val chinese = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_CHINESE))
            val romanization = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ROMANIZATION))
            val english = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ENGLISH))
            val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_DIFFICULTY))

            item = VocabularyItem(
                id = id,
                category = categoryId,
                chinese = chinese,
                romanization = romanization,
                english = english,
                difficulty = difficulty
            )
        }

        cursor.close()
        db.close()
        return item
    }

    fun updateProgress(userId: String, vocabularyId: String, status: Int) {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put(DatabaseManager.COLUMN_STATUS, status)
            put(DatabaseManager.COLUMN_LAST_REVIEWED, System.currentTimeMillis())
        }

        val whereClause = "${DatabaseManager.COLUMN_USER_ID} = ? AND ${DatabaseManager.COLUMN_VOCABULARY_ID} = ?"
        val whereArgs = arrayOf(userId, vocabularyId)

        val rowsAffected = db.update(DatabaseManager.TABLE_PROGRESS, values, whereClause, whereArgs)

        if (rowsAffected == 0) {
            // If no rows were updated, it means the entry doesn't exist, so insert it
            values.put(DatabaseManager.COLUMN_USER_ID, userId)
            values.put(DatabaseManager.COLUMN_VOCABULARY_ID, vocabularyId)
            db.insert(DatabaseManager.TABLE_PROGRESS, null, values)
        }

        db.close()
    }

    fun resetDatabase() {
        dbHelper.recreateDatabase()
    }
}