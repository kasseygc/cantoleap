package com.example.project.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseManager(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        const val DATABASE_NAME = "cantoleap.db"
        const val DATABASE_VERSION = 1

        // Table names
        const val TABLE_CATEGORIES = "categories"
        const val TABLE_VOCABULARY = "vocabulary"
        const val TABLE_PROGRESS = "progress"

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
        const val COLUMN_DIFFICULTY = "difficulty"

        // Progress table
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_VOCABULARY_ID = "vocabulary_id"
        const val COLUMN_STATUS = "status"
        const val COLUMN_LAST_REVIEWED = "last_reviewed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Skip table creation since we're using a pre-populated database from assets
        Log.d("DatabaseManager", "Using pre-populated database from assets, skipping table creation")

        // IMPORTANT: Create the progress table if it doesn't exist
        // This table isn't in the asset database but we need it for tracking user progress
        try {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS $TABLE_PROGRESS (
                    $COLUMN_USER_ID TEXT NOT NULL,
                    $COLUMN_VOCABULARY_ID TEXT NOT NULL,
                    $COLUMN_STATUS INTEGER DEFAULT 0,
                    $COLUMN_LAST_REVIEWED INTEGER,
                    PRIMARY KEY ($COLUMN_USER_ID, $COLUMN_VOCABULARY_ID)
                )
            """)
            Log.d("DatabaseManager", "Progress table created successfully")
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error creating progress table", e)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrades if needed
        Log.d("DatabaseManager", "Upgrading database from $oldVersion to $newVersion")

        // Only drop and recreate the progress table since it's the only one we manage programmatically
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")

        // Create the progress table again
        onCreate(db)
    }

    // Helper method to recreate database (useful for debugging)
    fun recreateDatabase() {
        // For a clean database reset, we'll just delete the progress table
        // since categories and vocabulary should come from the asset database
        val db = writableDatabase
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")

        // Create the progress table again
        onCreate(db)

        Log.d("DatabaseManager", "Progress data reset successfully")
    }
}