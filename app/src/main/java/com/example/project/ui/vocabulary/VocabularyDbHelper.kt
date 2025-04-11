// VocabularyDbHelper.kt
package com.example.project.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.project.data.models.VocabularyItem

class VocabularyDbHelper(context: Context) : SQLiteOpenHelper(
    context, DATABASE_NAME, null, DATABASE_VERSION
) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "CantoLeap.db"

        // Vocabulary table
        const val TABLE_VOCABULARY = "vocabulary"
        const val COLUMN_ID = "id"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_CHINESE = "chinese"
        const val COLUMN_ROMANIZATION = "romanization"
        const val COLUMN_ENGLISH = "english"
        const val COLUMN_AUDIO_REF = "audio_ref"

        // Progress table
        const val TABLE_PROGRESS = "progress"
        const val COLUMN_USER_ID = "user_id"
        const val COLUMN_VOCABULARY_ID = "vocabulary_id"
        const val COLUMN_STATUS = "status"  // 0 = not started, 1 = learning, 2 = known
        const val COLUMN_LAST_REVIEWED = "last_reviewed"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Create vocabulary table
        val createVocabularyTable = """
            CREATE TABLE $TABLE_VOCABULARY (
                $COLUMN_ID TEXT PRIMARY KEY,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_CHINESE TEXT NOT NULL,
                $COLUMN_ROMANIZATION TEXT NOT NULL,
                $COLUMN_ENGLISH TEXT NOT NULL,
                $COLUMN_AUDIO_REF TEXT NOT NULL
            )
        """.trimIndent()

        // Create progress table
        val createProgressTable = """
            CREATE TABLE $TABLE_PROGRESS (
                $COLUMN_USER_ID TEXT NOT NULL,
                $COLUMN_VOCABULARY_ID TEXT NOT NULL,
                $COLUMN_STATUS INTEGER NOT NULL DEFAULT 0,
                $COLUMN_LAST_REVIEWED INTEGER,
                PRIMARY KEY ($COLUMN_USER_ID, $COLUMN_VOCABULARY_ID)
            )
        """.trimIndent()

        db.execSQL(createVocabularyTable)
        db.execSQL(createProgressTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_VOCABULARY")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_PROGRESS")
        onCreate(db)
    }

    // Insert initial vocabulary data
    fun populateInitialData() {
        // Check if vocabulary already exists
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_VOCABULARY", null)
        cursor.moveToFirst()
        val count = cursor.getInt(0)
        cursor.close()

        // If data already exists, don't add again
        if (count > 0) return

        // Add sample vocabulary items
        val initialVocabulary = getSampleVocabularyItems()
        val writableDb = writableDatabase

        writableDb.beginTransaction()
        try {
            for (item in initialVocabulary) {
                val values = ContentValues().apply {
                    put(COLUMN_ID, item.id)
                    put(COLUMN_CATEGORY, item.category)
                    put(COLUMN_CHINESE, item.chinese)
                    put(COLUMN_ROMANIZATION, item.romanization)
                    put(COLUMN_ENGLISH, item.english)
                    put(COLUMN_AUDIO_REF, item.audioRef)
                }
                writableDb.insert(TABLE_VOCABULARY, null, values)
            }
            writableDb.setTransactionSuccessful()
        } finally {
            writableDb.endTransaction()
        }
    }

    // Sample data for initial setup
    private fun getSampleVocabularyItems(): List<VocabularyItem> {
        return listOf(
            // Greetings
            VocabularyItem("greet_1", "Greetings", "你好", "nei5 hou2", "Hello", "hello"),
            VocabularyItem("greet_2", "Greetings", "早晨", "zou2 san4", "Good morning", "good_morning"),
            VocabularyItem("greet_3", "Greetings", "多謝", "do1 ze6", "Thank you", "thank_you"),
            VocabularyItem("greet_4", "Greetings", "再見", "zoi3 gin3", "Goodbye", "goodbye"),
            VocabularyItem("greet_5", "Greetings", "對唔住", "deoi3 m4 zyu6", "Sorry", "sorry"),

            // Family
            VocabularyItem("family_1", "Family", "媽媽", "maa1 maa1", "Mother", "mother"),
            VocabularyItem("family_2", "Family", "爸爸", "baa4 baa1", "Father", "father"),
            VocabularyItem("family_3", "Family", "哥哥", "go4 go1", "Elder brother", "elder_brother"),
            VocabularyItem("family_4", "Family", "姐姐", "ze2 ze1", "Elder sister", "elder_sister"),
            VocabularyItem("family_5", "Family", "弟弟", "dai6 dai2", "Younger brother", "younger_brother"),

            // Numbers
            VocabularyItem("num_1", "Numbers", "一", "jat1", "One", "one"),
            VocabularyItem("num_2", "Numbers", "二", "ji6", "Two", "two"),
            VocabularyItem("num_3", "Numbers", "三", "saam1", "Three", "three"),
            VocabularyItem("num_4", "Numbers", "四", "sei3", "Four", "four"),
            VocabularyItem("num_5", "Numbers", "五", "ng5", "Five", "five"),

            // Food
            VocabularyItem("food_1", "Food", "飯", "faan6", "Rice", "rice"),
            VocabularyItem("food_2", "Food", "麵", "min6", "Noodles", "noodles"),
            VocabularyItem("food_3", "Food", "水", "seoi2", "Water", "water"),
            VocabularyItem("food_4", "Food", "茶", "caa4", "Tea", "tea"),
            VocabularyItem("food_5", "Food", "咖啡", "gaa3 fe1", "Coffee", "coffee"),

            // Useful phrases
            VocabularyItem("phrase_1", "Useful phrases", "唔該", "m4 goi1", "Please/Excuse me", "please"),
            VocabularyItem("phrase_2", "Useful phrases", "係", "hai6", "Yes/It is", "yes"),
            VocabularyItem("phrase_3", "Useful phrases", "唔係", "m4 hai6", "No/It isn't", "no"),
            VocabularyItem("phrase_4", "Useful phrases", "幾多錢", "gei2 do1 cin2", "How much?", "how_much"),
            VocabularyItem("phrase_5", "Useful phrases", "我唔明白", "ngo5 m4 ming4 baak6", "I don't understand", "dont_understand")
        )
    }
}