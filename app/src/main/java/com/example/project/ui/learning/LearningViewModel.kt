package com.example.project.ui.learning

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.data.models.VocabularyItem

class LearningViewModel : ViewModel() {

    private val _currentWord = MutableLiveData<VocabularyItem>().apply {
        // Sample data for testing
        value = VocabularyItem(
            "1",
            "Greetings",
            "你好",
            "nei5 hou2",
            "Hello",
            "audio_hello"
        )
    }

    val currentWord: LiveData<VocabularyItem> = _currentWord

    private val _progress = MutableLiveData<Int>().apply {
        value = 1
    }

    val progress: LiveData<Int> = _progress

    private val _totalWords = MutableLiveData<Int>().apply {
        value = 20
    }

    val totalWords: LiveData<Int> = _totalWords

    // Sample vocabulary for testing
    private val sampleVocabulary = listOf(
        VocabularyItem("1", "Greetings", "你好", "nei5 hou2", "Hello", "audio_hello"),
        VocabularyItem("2", "Greetings", "早晨", "zou2 san4", "Good morning", "audio_morning"),
        VocabularyItem("3", "Greetings", "再見", "zoi3 gin3", "Goodbye", "audio_goodbye")
    )

    private var currentIndex = 0

    fun moveToNextWord() {
        currentIndex = (currentIndex + 1) % sampleVocabulary.size
        _currentWord.value = sampleVocabulary[currentIndex]
        _progress.value = currentIndex + 1
    }

    fun markAsKnown() {
        // Will implement progress tracking later
    }
}