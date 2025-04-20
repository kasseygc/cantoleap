package com.example.project.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

private const val DATABASE_NAME = "cantoleap.db"

class DatabaseInitializer(private val context: Context) {

    // Declare assetDbPath and dbPath here
    private val assetDbPath = "cantoleap.db"
    private val dbPath = context.getDatabasePath(DATABASE_NAME).path

    fun initializeDatabase(): Boolean {
        val dbFile = File(dbPath)

        // If database already exists, verify it
        if (dbFile.exists()) {
            Log.d("DatabaseInitializer", "Database already exists at: $dbPath")

            // Verify the database has the required tables
            if (verifyDatabase()) {
                return true
            } else {
                // If verification fails, delete and try again
                Log.w("DatabaseInitializer", "Database verification failed, recreating")
                dbFile.delete()
            }
        }

        // Make sure the directory exists
        dbFile.parentFile?.mkdirs()

        try {
            // Copy database from assets
            val inputStream = context.assets.open(assetDbPath)
            val outputStream = FileOutputStream(dbFile)

            val buffer = ByteArray(1024)
            var length: Int
            var totalBytes = 0

            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
                totalBytes += length
            }

            inputStream.close()
            outputStream.flush()
            outputStream.close()

            Log.d("DatabaseInitializer", "Database copied successfully. Size: $totalBytes bytes")

            // Verify the database after copying
            return verifyDatabase()
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Error copying database", e)
            return false
        }
    }

    private fun verifyDatabase(): Boolean {
        try {
            val db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

            // Check if the categories table exists
            val cursor = db.rawQuery(
                "SELECT name FROM sqlite_master WHERE type='table' AND name='categories'",
                null
            )

            val hasCategories = cursor.count > 0
            cursor.close()

            if (!hasCategories) {
                Log.e("DatabaseInitializer", "Database verification failed: categories table not found")
                return false
            }

            // Count categories to make sure there's data
            val countCursor = db.rawQuery("SELECT COUNT(*) FROM categories", null)
            countCursor.moveToFirst()
            val count = countCursor.getInt(0)
            countCursor.close()

            db.close()

            if (count == 0) {
                Log.e("DatabaseInitializer", "Database verification failed: categories table is empty")
                return false
            }

            Log.d("DatabaseInitializer", "Database verification successful: found $count categories")
            return true
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Database verification failed with exception", e)
            return false
        }
    }
}