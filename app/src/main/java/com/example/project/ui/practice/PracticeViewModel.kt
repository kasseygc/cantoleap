package com.example.project.ui.practice

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.data.models.QuizQuestion
import com.example.project.data.models.VocabularyItem
import com.example.project.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = VocabularyRepository(application)
    private var quizQuestions: List<QuizQuestion> = emptyList()

    private val _currentQuestionIndex = MutableLiveData<Int>().apply {
        value = 0
    }
    val currentQuestionIndex: LiveData<Int> = _currentQuestionIndex

    private val _totalQuestions = MutableLiveData<Int>().apply {
        value = 0
    }
    val totalQuestions: LiveData<Int> = _totalQuestions

    private val _currentQuestion = MutableLiveData<QuizQuestion>().apply {
        // Sample data for testing
        value = QuizQuestion(
            "男仔",
            listOf("Man", "Woman", "Boy", "Girl"),
            2,
            "1"
        )
    }

    val currentQuestion: LiveData<QuizQuestion> = _currentQuestion

    private val _score = MutableLiveData<Int>().apply {
        value = 0
    }

    val score: LiveData<Int> = _score

    private val _isQuizComplete = MutableLiveData<Boolean>().apply {
        value = false
    }

    val isQuizComplete: LiveData<Boolean> = _isQuizComplete

    private var selectedAnswerIndex: Int? = null

    fun selectAnswer(index: Int) {
        selectedAnswerIndex = index
    }

    fun isAnswerSelected(): Boolean {
        return selectedAnswerIndex != null
    }

    fun submitAnswer(selectedIndex: Int) {
        val currentQuestion = _currentQuestion.value ?: return

        if (selectedIndex == currentQuestion.correctAnswerIndex) {
            _score.value = (_score.value ?: 0) + 1
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

    fun startQuiz(category: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val vocabularyItems = repository.getVocabularyByCategory(category)
            val questions = generateQuizQuestions(vocabularyItems)

            withContext(Dispatchers.Main) {
                quizQuestions = questions
                _totalQuestions.value = questions.size
                _score.value = 0
                _isQuizComplete.value = false

                if (questions.isNotEmpty()) {
                    _currentQuestionIndex.value = 0
                    _currentQuestion.value = questions[0]
                }
            }
        }
    }

    fun startMixedQuiz() {
        viewModelScope.launch(Dispatchers.IO) {
            // Get all categories
            val categories = repository.getCategories()
            val allItems = mutableListOf<VocabularyItem>()

            // Get vocabulary items from each category
            for (category in categories) {
                val items = repository.getVocabularyByCategory(category.id)
                allItems.addAll(items)
            }

            // Create quiz questions from all items
            val questions = generateQuizQuestions(allItems)

            withContext(Dispatchers.Main) {
                quizQuestions = questions
                _totalQuestions.value = questions.size
                _score.value = 0
                _isQuizComplete.value = false

                if (questions.isNotEmpty()) {
                    _currentQuestionIndex.value = 0
                    _currentQuestion.value = questions[0]
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

    // Generate quiz questions from vocabulary items
    private fun generateQuizQuestions(vocabularyItems: List<VocabularyItem>): List<QuizQuestion> {
        if (vocabularyItems.isEmpty()) return emptyList()

        // Filter out items with insufficient data
        val validItems = vocabularyItems.filter {
            it.chinese.isNotEmpty() && it.english.isNotEmpty()
        }

        if (validItems.size < 4) return emptyList() // Need at least 4 items for multiple choice

        // Create questions (up to 10)
        val questionItems = validItems.shuffled().take(10)
        return questionItems.mapIndexed { index, item ->
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
}