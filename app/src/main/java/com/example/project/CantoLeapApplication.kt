package com.example.project

import android.app.Application
import android.util.Log
import com.example.project.data.local.DatabaseInitializer
import com.google.firebase.FirebaseApp

class CantoLeapApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        try {
            // Initialize Firebase first
            FirebaseApp.initializeApp(this)
            Log.d("CantoLeapApp", "Firebase initialized")

            // Initialize database
            val dbInitializer = DatabaseInitializer(this)
            dbInitializer.initializeDatabase()
            Log.d("CantoLeapApp", "Database initialized")
        } catch (e: Exception) {
            Log.e("CantoLeapApp", "Error during initialization", e)
        }
    }
}