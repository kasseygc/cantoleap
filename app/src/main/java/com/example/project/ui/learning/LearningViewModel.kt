package com.example.project.ui.learning

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.data.models.VocabularyItem
import com.example.project.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LearningViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabularyRepository(application)

    private val _currentWord = MutableLiveData<VocabularyItem>().apply {
        // Sample data for testing
        value = VocabularyItem(
            "1",
            "Greetings",
            "你好",
            "nei5 hou2",
            "Hello",
            "audio_hello",
            1
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
    private var vocabularyItems = listOf(
        VocabularyItem("1", "Greetings", "你好", "nei5 hou2", "Hello", "audio_hello", 1),
        VocabularyItem("2", "Greetings", "早晨", "zou2 san4", "Good morning", "audio_morning", 1),
        VocabularyItem("3", "Greetings", "再見", "zoi3 gin3", "Goodbye", "audio_goodbye", 1)
    )

    private var currentIndex = 0

    fun moveToNextWord() {
        if (vocabularyItems.isEmpty()) return

        currentIndex = (currentIndex + 1) % vocabularyItems.size
        _currentWord.value = vocabularyItems[currentIndex]
        _progress.value = currentIndex + 1
    }

    fun markAsKnown() {
        // Will implement progress tracking later
    }

    fun loadCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val items = repository.getVocabularyByCategory(category)

            withContext(Dispatchers.Main) {
                vocabularyItems = items
                _totalWords.value = items.size

                if (items.isNotEmpty()) {
                    currentIndex = 0
                    _currentWord.value = items[currentIndex]
                    _progress.value = currentIndex + 1
                }
            }
        }
    }
}