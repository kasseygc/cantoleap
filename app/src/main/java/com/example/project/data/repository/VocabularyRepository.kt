package com.example.project.data.repository

import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.project.data.local.DatabaseManager
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.models.VocabularyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class VocabularyRepository(application: Application) {

    private val dbManager = DatabaseManager(application)

    fun getVocabularyByCategory(categoryId: String): List<VocabularyItem> {
        val items = mutableListOf<VocabularyItem>()
        val db = dbManager.readableDatabase

        try {
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
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error getting vocabulary by category", e)
        } finally {
            db.close()
        }

        return items
    }

    fun getVocabularyById(id: String): VocabularyItem? {
        val db = dbManager.readableDatabase
        var item: VocabularyItem? = null

        try {
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
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error getting vocabulary by id", e)
        } finally {
            db.close()
        }

        return item
    }

    fun getAllVocabularyItems(): List<VocabularyItem> {
        val items = mutableListOf<VocabularyItem>()
        val db = dbManager.readableDatabase

        try {
            val cursor = db.query(
                DatabaseManager.TABLE_VOCABULARY,
                null,
                null,
                null,
                null,
                null,
                DatabaseManager.COLUMN_DIFFICULTY
            )

            if (cursor.moveToFirst()) {
                do {
                    val id = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ID))
                    val category = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_CATEGORY_ID))
                    val chinese = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_CHINESE))
                    val romanization = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ROMANIZATION))
                    val english = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_ENGLISH))
                    val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_DIFFICULTY))

                    items.add(
                        VocabularyItem(
                            id = id,
                            category = category,
                            chinese = chinese,
                            romanization = romanization,
                            english = english,
                            difficulty = difficulty
                        )
                    )
                } while (cursor.moveToNext())
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error getting all vocabulary items", e)
        } finally {
            db.close()
        }

        return items
    }

    fun getCategories(): List<VocabularyCategory> {
        val categories = mutableListOf<VocabularyCategory>()
        val db = dbManager.readableDatabase

        try {
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
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error getting categories", e)
        } finally {
            db.close()
        }

        return categories
    }

    private fun getWordCountForCategory(categoryId: String): Int {
        val db = dbManager.readableDatabase
        var count = 0

        try {
            val query = "SELECT COUNT(*) FROM ${DatabaseManager.TABLE_VOCABULARY} WHERE ${DatabaseManager.COLUMN_CATEGORY_ID} = ?"
            val cursor = db.rawQuery(query, arrayOf(categoryId))

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error getting word count", e)
        }

        return count
    }

    fun resetDatabase() {
        try {
            dbManager.recreateDatabase()
        } catch (e: Exception) {
            Log.e("VocabularyRepository", "Error resetting database", e)
        }
    }

    suspend fun updateProgress(userId: String, itemId: String, status: Int) {
        withContext(Dispatchers.IO) {
            val db = dbManager.writableDatabase

            try {
                val values = ContentValues().apply {
                    put(DatabaseManager.COLUMN_STATUS, status)
                    put(DatabaseManager.COLUMN_LAST_REVIEWED, System.currentTimeMillis())
                }

                val whereClause = "${DatabaseManager.COLUMN_USER_ID} = ? AND ${DatabaseManager.COLUMN_VOCABULARY_ID} = ?"
                val whereArgs = arrayOf(userId, itemId)

                val rowsAffected = db.update(DatabaseManager.TABLE_PROGRESS, values, whereClause, whereArgs)

                if (rowsAffected > 0) {
                    Log.d("VocabularyRepository", "Progress updated successfully for $userId, $itemId")
                } else {
                    // If no rows were updated, it might mean the entry doesn't exist, so insert it
                    values.put(DatabaseManager.COLUMN_USER_ID, userId)
                    values.put(DatabaseManager.COLUMN_VOCABULARY_ID, itemId)
                    db.insert(DatabaseManager.TABLE_PROGRESS, null, values)
                    Log.d("VocabularyRepository", "Progress entry created for $userId, $itemId")
                }

            } catch (e: Exception) {
                Log.e("VocabularyRepository", "Error updating progress", e)
            } finally {
                db.close()
            }
        }
    }

    fun getUserProgress(userId: String): LiveData<Map<String, Int>> {
        val progressLiveData = MutableLiveData<Map<String, Int>>()

        runBlocking(Dispatchers.IO) {
            val db = dbManager.readableDatabase
            val progressMap = mutableMapOf<String, Int>()

            try {
                val cursor = db.query(
                    DatabaseManager.TABLE_PROGRESS,
                    arrayOf(DatabaseManager.COLUMN_VOCABULARY_ID, DatabaseManager.COLUMN_STATUS),
                    "${DatabaseManager.COLUMN_USER_ID} = ?",
                    arrayOf(userId),
                    null,
                    null,
                    null
                )

                if (cursor.moveToFirst()) {
                    do {
                        val itemId = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_VOCABULARY_ID))
                        val status = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseManager.COLUMN_STATUS))
                        progressMap[itemId] = status
                    } while (cursor.moveToNext())
                }

                cursor.close()
                withContext(Dispatchers.Main) {
                    progressLiveData.value = progressMap
                }

            } catch (e: Exception) {
                Log.e("VocabularyRepository", "Error getting user progress", e)
            } finally {
                db.close()
            }
        }

        return progressLiveData
    }
}