package com.example.project.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.data.models.QuizQuestion
import com.example.project.data.models.VocabularyItem
import com.example.project.data.repository.DataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PracticeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application)

    private val _currentQuestion = MutableLiveData<QuizQuestion>()
    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    private val _currentQuestionIndex = MutableLiveData<Int>(0)
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _totalQuestions = MutableLiveData<Int>(0)
    val totalQuestions: LiveData<Int> = _totalQuestions

    private val _score = MutableLiveData<Int>(0)
    val score: LiveData<Int> = _score

    private val _isQuizComplete = MutableLiveData<Boolean>(false)
    val isQuizComplete: LiveData<Boolean> = _isQuizComplete

    private val _loading = MutableLiveData<Boolean>(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    private var quizQuestions = listOf<QuizQuestion>()
    private var selectedAnswerIndex: Int? = null

    // User ID (replace with actual user authentication logic)
    private val userId = "defaultUser"

    fun startQuiz(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val vocabularyItems = repository.getVocabularyByCategory(category)
                val questions = generateQuizQuestions(vocabularyItems)

                withContext(Dispatchers.Main) {
                    if (questions.isNotEmpty()) {
                        quizQuestions = questions
                        _currentQuestionIndex.value = 0
                        _currentQuestion.value = questions[0]
                        _totalQuestions.value = questions.size
                        _score.value = 0
                        _isQuizComplete.value = false
                        _error.value = null
                    } else {
                        _error.value = "Not enough vocabulary items to create a quiz"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error creating quiz: ${e.message}"
                }
            } finally {
                _loading.postValue(false)
            }
        }
    }

    fun startMixedQuiz() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val categories = repository.getCategories()
                val allItems = mutableListOf<VocabularyItem>()

                for (category in categories) {
                    val items = repository.getVocabularyByCategory(category.id)
                    allItems.addAll(items)
                }

                val questions = generateQuizQuestions(allItems)

                withContext(Dispatchers.Main) {
                    if (questions.isNotEmpty()) {
                        quizQuestions = questions
                        _currentQuestionIndex.value = 0
                        _currentQuestion.value = questions[0]
                        _totalQuestions.value = questions.size
                        _score.value = 0
                        _isQuizComplete.value = false
                        _error.value = null
                    } else {
                        _error.value = "Not enough vocabulary items to create a quiz"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _error.value = "Error creating quiz: ${e.message}"
                }
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private fun generateQuizQuestions(vocabularyItems: List<VocabularyItem>): List<QuizQuestion> {
        if (vocabularyItems.size < 4) return emptyList()

        val validItems = vocabularyItems.filter {
            it.chinese.isNotEmpty() && it.english.isNotEmpty()
        }

        if (validItems.size < 4) return emptyList()

        // Take up to 10 items for the quiz
        val questionItems = validItems.shuffled().take(10)

        return questionItems.map { item ->
            // For each question, get 3 incorrect options
            val otherItems = validItems.filter { it.id != item.id }.shuffled().take(3)
            val options = (otherItems.map { it.english } + item.english).shuffled()

            QuizQuestion(
                prompt = item.chinese,
                options = options,
                correctAnswerIndex = options.indexOf(item.english),
                vocabularyItemId = item.id
            )
        }
    }

    fun getVocabularyDetails(vocabularyId: String, callback: (VocabularyItem?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val item = repository.getVocabularyById(vocabularyId)
            withContext(Dispatchers.Main) {
                callback(item)
            }
        }
    }

    fun submitAnswer(selectedIndex: Int) {
        selectedAnswerIndex = selectedIndex

        val currentQuestion = _currentQuestion.value ?: return
        val currentScore = _score.value ?: 0

        if (selectedIndex == currentQuestion.correctAnswerIndex) {
            _score.value = currentScore + 1

            // Update progress locally
            viewModelScope.launch {
                try {
                    repository.updateProgress(userId, currentQuestion.vocabularyItemId, 1)
                } catch (e: Exception) {
                    // Just continue if update fails
                }
            }
        }
    }

    fun moveToNextQuestion() {
        val currentIndex = _currentQuestionIndex.value ?: 0
        val nextIndex = currentIndex + 1

        if (nextIndex < quizQuestions.size) {
            _currentQuestionIndex.value = nextIndex
            _currentQuestion.value = quizQuestions[nextIndex]
            selectedAnswerIndex = null
        } else {
            _isQuizComplete.value = true
        }
    }
}