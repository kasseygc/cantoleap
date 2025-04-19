package com.example.project.data.local

import android.content.Context
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

        // Skip if database already exists
        if (dbFile.exists()) {
            Log.d("DatabaseInitializer", "Database already exists at: $dbPath")
            // Check file size to make sure it's not empty
            if (dbFile.length() > 0) {
                return true;
            } else {
                Log.w("DatabaseInitializer", "Database exists but is empty, will try to recreate");
                dbFile.delete();
            }
        }

        // Make sure the directory exists
        dbFile.parentFile?.mkdirs()

        try {
            // Check if asset exists with more detailed logging
            val assets = context.assets
            val assetsList = assets.list("") ?: emptyArray()

            if (!assetsList.contains(assetDbPath)) {
                Log.e("DatabaseInitializer", "Database asset not found: $assetDbPath")
                Log.d("DatabaseInitializer", "Available assets: ${assetsList.joinToString()}")
                return false
            }

            Log.d("DatabaseInitializer", "Found database asset: $assetDbPath")

            // Copy database from assets
            val inputStream = assets.open(assetDbPath)
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

            // Verify the file was copied correctly
            if (dbFile.exists() && dbFile.length() > 0) {
                return true
            } else {
                Log.e("DatabaseInitializer", "Database file not created properly after copy")
                return false
            }
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Error copying database", e)
            return false
        }
    }

    // Helper function to delete existing database (useful for debugging)
    fun deleteExistingDatabase(): Boolean {
        val dbFile = File(dbPath)
        val journalFile = File("$dbPath-journal")

        var success = true

        if (dbFile.exists()) {
            success = dbFile.delete() && success
            Log.d("DatabaseInitializer", "Deleted existing database: $success")
        }

        if (journalFile.exists()) {
            success = journalFile.delete() && success
            Log.d("DatabaseInitializer", "Deleted journal file: $success")
        }

        return success
    }
}