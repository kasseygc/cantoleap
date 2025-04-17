package com.example.project

import android.app.Application
import com.example.project.data.local.DatabaseInitializer

class CantoLeapApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize database
        val dbInitializer = DatabaseInitializer(this)
        dbInitializer.initializeDatabase()
    }
}