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

    private val _currentWord = MutableLiveData<VocabularyItem>()
    val currentWord: LiveData<VocabularyItem> = _currentWord

    private val _progress = MutableLiveData<Int>(0)
    val progress: LiveData<Int> = _progress

    private val _totalWords = MutableLiveData<Int>(0)
    val totalWords: LiveData<Int> = _totalWords

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Current vocabulary ID for notes
    private val _currentVocabularyId = MutableLiveData<String>()
    val currentVocabularyId: LiveData<String> = _currentVocabularyId

    // List of vocabulary items in current category
    private var vocabularyItems = listOf<VocabularyItem>()
    private var currentIndex = 0

    // User ID (replace with actual user authentication logic)
    private val userId = "defaultUser"

    fun loadCategory(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val items = repository.getVocabularyByCategory(category)

                withContext(Dispatchers.Main) {
                    if (items.isNotEmpty()) {
                        vocabularyItems = items
                        currentIndex = 0
                        _currentWord.value = items[0]
                        _currentVocabularyId.value = items[0].id
                        _progress.value = 1
                        _totalWords.value = items.size
                        _error.value = null
                    } else {
                        _error.value = "No vocabulary items found for this category"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error loading vocabulary: ${e.message}"
                }
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun moveToNextWord() {
        if (vocabularyItems.isEmpty()) return

        currentIndex = (currentIndex + 1) % vocabularyItems.size
        _currentWord.value = vocabularyItems[currentIndex]
        _currentVocabularyId.value = vocabularyItems[currentIndex].id
        _progress.value = currentIndex + 1
    }

    fun markAsKnown() {
        _currentWord.value?.let { word ->
            // Simply track it locally for now
            viewModelScope.launch {
                try {
                    repository.updateProgress(userId, word.id, 1)
                } catch (e: Exception) {
                    // Just continue if update fails
                }
                moveToNextWord()
            }
        }
    }
}