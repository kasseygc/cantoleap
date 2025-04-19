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
        const val TABLE_CATEGORIES = "vocabulary_categories"
        const val TABLE_VOCABULARY = "vocabulary_items"

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
    }

    private val dbInitializer = DatabaseInitializer(context)

    init {
        // Make sure database is initialized
        val success = dbInitializer.initializeDatabase()

        if (!success) {
            Log.e("DatabaseManager", "Failed to initialize database from assets")
        }

        // Verify database has tables and debug
        debugDatabaseContent()
    }

    fun debugDatabaseContent() {
        try {
            val db = readableDatabase

            // Check categories
            var categoriesCount = 0
            try {
                val categoriesCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_CATEGORIES", null)
                if (categoriesCursor.moveToFirst()) {
                    categoriesCount = categoriesCursor.getInt(0)
                }
                categoriesCursor.close()
                Log.d("DatabaseManager", "Categories table exists with $categoriesCount entries")
            } catch (e: Exception) {
                Log.e("DatabaseManager", "Categories table doesn't exist or is corrupt", e)
                // No need to create tables here, as DatabaseInitializer should have handled it
                return
            }

            // Check vocabulary
            var vocabCount = 0
            try {
                val vocabCursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_VOCABULARY", null)
                if (vocabCursor.moveToFirst()) {
                    vocabCount = vocabCursor.getInt(0)
                }
                vocabCursor.close()
                Log.d("DatabaseManager", "Vocabulary table exists with $vocabCount entries")
            } catch (e: Exception) {
                Log.e("DatabaseManager", "Vocabulary table doesn't exist or is corrupt", e)
                // No need to create tables here, as DatabaseInitializer should have handled it
                return
            }

            // If tables exist but are empty, the database might be corrupted or improperly created
            if (categoriesCount == 0 || vocabCount == 0) {
                Log.w("DatabaseManager", "Database tables exist but appear to be empty!")
            }

            // Sample first category
            try {
                val sampleCursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES LIMIT 1", null)
                if (sampleCursor.moveToFirst()) {
                    val id = sampleCursor.getString(sampleCursor.getColumnIndexOrThrow(COLUMN_ID))
                    val name = sampleCursor.getString(sampleCursor.getColumnIndexOrThrow(COLUMN_NAME))
                    Log.d("DatabaseManager", "Sample category: $id - $name")
                }
                sampleCursor.close()
            } catch (e: Exception) {
                Log.e("DatabaseManager", "Error sampling category data", e)
            }

        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error debugging database", e)
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        Log.d("DatabaseManager", "Creating database tables from scratch")
        createTables(db)
    }

    private fun createTables(db: SQLiteDatabase) {
        // Create categories table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_CATEGORIES (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_NAME TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT,
                $COLUMN_ICON_NAME TEXT
            )
        """)

        // Create vocabulary table
        db.execSQL("""
            CREATE TABLE IF NOT EXISTS $TABLE_VOCABULARY (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_CATEGORY_ID TEXT NOT NULL,
                $COLUMN_CHINESE TEXT NOT NULL,
                $COLUMN_ROMANIZATION TEXT NOT NULL,
                $COLUMN_ENGLISH TEXT NOT NULL,
                $COLUMN_DIFFICULTY INTEGER DEFAULT 1,
                FOREIGN KEY($COLUMN_CATEGORY_ID) REFERENCES $TABLE_CATEGORIES($COLUMN_ID)
            )
        """)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // For future database upgrades
        Log.d("DatabaseManager", "Upgrading database from version $oldVersion to $newVersion")
    }

    // Reset database function - useful for debugging
    fun resetDatabase() {
        dbInitializer.deleteExistingDatabase()
        dbInitializer.initializeDatabase()
        debugDatabaseContent()
    }

    // Get all vocabulary categories
    fun getCategories(): List<VocabularyCategory> {
        val categories = mutableListOf<VocabularyCategory>()
        val db = readableDatabase

        try {
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
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error getting categories", e)
        }

        return categories
    }

    // Get word count for a category
    private fun getWordCountForCategory(categoryId: String): Int {
        val db = readableDatabase

        var count = 0
        try {
            val query = "SELECT COUNT(*) FROM $TABLE_VOCABULARY WHERE $COLUMN_CATEGORY_ID = ?"
            val cursor = db.rawQuery(query, arrayOf(categoryId))

            if (cursor.moveToFirst()) {
                count = cursor.getInt(0)
            }

            cursor.close()
        } catch (e: Exception) {
            Log.e("DatabaseManager", "Error getting word count for category $categoryId", e)
        }

        return count
    }

    // Get vocabulary items for a category
    fun getVocabularyByCategory(categoryId: String): List<VocabularyItem> {
        val items = mutableListOf<VocabularyItem>()
        val db = readableDatabase

        try {
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
                    val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY))

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
            Log.e("DatabaseManager", "Error getting vocabulary for category $categoryId", e)
        }

        return items
    }

    // Get a single vocabulary item by ID
    fun getVocabularyById(id: String): VocabularyItem? {
        val db = readableDatabase
        var item: VocabularyItem? = null

        try {
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

            if (cursor.moveToFirst()) {
                val categoryId = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY_ID))
                val chinese = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CHINESE))
                val romanization = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROMANIZATION))
                val english = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ENGLISH))
                val difficulty = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DIFFICULTY))

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
            Log.e("DatabaseManager", "Error getting vocabulary item $id", e)
        }

        return item
    }
}