package com.example.project.data.repository

import android.app.Application
import android.content.ContentValues
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.project.data.local.DatabaseManager
import com.example.project.data.models.VocabularyItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class ProgressRepository(application: Application) {
    private val dbManager = DatabaseManager(application)

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
                Log.e("ProgressRepository", "Error getting user progress", e)
            } finally {
                db.close()
            }
        }

        return progressLiveData
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
                    Log.d("ProgressRepository", "Progress updated successfully for $userId, $itemId")
                } else {
                    // If no rows were updated, it might mean the entry doesn't exist, so insert it
                    values.put(DatabaseManager.COLUMN_USER_ID, userId)
                    values.put(DatabaseManager.COLUMN_VOCABULARY_ID, itemId)
                    db.insert(DatabaseManager.TABLE_PROGRESS, null, values)
                    Log.d("ProgressRepository", "Progress entry created for $userId, $itemId")
                }

            } catch (e: Exception) {
                Log.e("ProgressRepository", "Error updating progress", e)
            } finally {
                db.close()
            }
        }
    }
}