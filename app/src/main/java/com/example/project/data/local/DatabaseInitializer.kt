// DatabaseInitializer.kt
package com.example.project.data.local

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class DatabaseInitializer(private val context: Context) {
    private val dbPath = context.getDatabasePath("cantoleap.db").path
    private val assetDbPath = "cantoleap.db"

    fun initializeDatabase(): Boolean {
        val dbFile = File(dbPath)

        // Skip if database already exists
        if (dbFile.exists()) {
            return true
        }

        // Make sure the directory exists
        dbFile.parentFile?.mkdirs()

        try {
            // Copy database from assets
            val inputStream = context.assets.open(assetDbPath)
            val outputStream = FileOutputStream(dbFile)

            inputStream.copyTo(outputStream)

            inputStream.close()
            outputStream.close()

            Log.d("DatabaseInitializer", "Database copied successfully")
            return true
        } catch (e: Exception) {
            Log.e("DatabaseInitializer", "Error copying database", e)
            return false
        }
    }
}