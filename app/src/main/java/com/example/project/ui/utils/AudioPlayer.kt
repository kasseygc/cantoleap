package com.example.project.utils

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import java.io.File

class AudioPlayer(private val context: Context) {
    private var mediaPlayer: MediaPlayer? = null
    private val audioCacheManager = AudioCacheManager(context)

    fun playAudio(audioRef: String, onStarted: () -> Unit, onError: () -> Unit, onCompleted: () -> Unit) {
        // Release any existing player
        releaseMediaPlayer()

        // Create new player
        mediaPlayer = MediaPlayer()

        // Set up completion listener
        mediaPlayer?.setOnCompletionListener {
            onCompleted()
        }

        // Get audio file from cache or download
        audioCacheManager.getAudioFile(audioRef) { file: File? ->
            if (file != null) {
                try {
                    mediaPlayer?.setDataSource(file.path)
                    mediaPlayer?.prepare()
                    mediaPlayer?.start()
                    onStarted()
                } catch (e: Exception) {
                    Log.e("AudioPlayer", "Error playing audio: ${e.message}")
                    onError()
                    releaseMediaPlayer()
                }
            } else {
                // Failed to get the file
                Log.e("AudioPlayer", "Failed to get audio file: $audioRef")
                onError()
                releaseMediaPlayer()
            }
        }
    }

    fun releaseMediaPlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
}