package com.example.project.utils

import android.content.Context
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.util.concurrent.ConcurrentHashMap

class AudioCacheManager(private val context: Context) {
    private val cacheDir = File(context.cacheDir, "audio_cache")
    private val downloadTasks = ConcurrentHashMap<String, Boolean>()

    init {
        // Create cache directory if it doesn't exist
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun getAudioFile(audioRef: String, callback: (File?) -> Unit) {
        val cachedFile = File(cacheDir, "$audioRef.mp3")

        // Check if file exists in cache
        if (cachedFile.exists() && cachedFile.length() > 0) {
            // Use cached file
            Log.d("AudioCache", "Using cached file for $audioRef")
            callback(cachedFile)
            return
        }

        // Check if download already in progress
        if (downloadTasks[audioRef] == true) {
            Log.d("AudioCache", "Download already in progress for $audioRef")
            // Wait for ongoing download
            waitForDownload(audioRef, cachedFile, callback)
            return
        }

        // Mark download as in progress
        downloadTasks[audioRef] = true

        // Download from Firebase
        downloadAndCacheFile(audioRef, cachedFile, callback)
    }

    private fun waitForDownload(audioRef: String, cachedFile: File, callback: (File?) -> Unit) {
        // Poll for file completion periodically
        android.os.Handler().postDelayed({
            if (cachedFile.exists() && cachedFile.length() > 0) {
                callback(cachedFile)
            } else if (downloadTasks[audioRef] == true) {
                // Keep waiting
                waitForDownload(audioRef, cachedFile, callback)
            } else {
                // Download failed or was canceled
                callback(null)
            }
        }, 500) // Check every 500ms
    }

    private fun downloadAndCacheFile(audioRef: String, localFile: File, callback: (File?) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val audioFileRef = storageRef.child("audio/$audioRef.mp3")

        Log.d("AudioCache", "Downloading audio file: $audioRef")

        audioFileRef.getFile(localFile)
            .addOnSuccessListener {
                Log.d("AudioCache", "Download successful for $audioRef")
                downloadTasks.remove(audioRef)
                callback(localFile)
            }
            .addOnFailureListener { exception ->
                Log.e("AudioCache", "Download failed for $audioRef: ${exception.message}")
                downloadTasks.remove(audioRef)
                localFile.delete() // Delete partial file
                callback(null)
            }
    }

    fun clearCache() {
        cacheDir.listFiles()?.forEach { it.delete() }
    }
}