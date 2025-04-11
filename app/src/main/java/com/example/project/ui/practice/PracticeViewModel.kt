package com.example.project.ui.practice

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.data.models.QuizQuestion

class PracticeViewModel : ViewModel() {

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

    fun submitAnswer() {
        selectedAnswerIndex?.let { selected ->
            if (selected == _currentQuestion.value?.correctAnswerIndex) {
                _score.value = (_score.value ?: 0) + 1
            }

            // We'll implement more quiz logic later

            // Reset selected answer
            selectedAnswerIndex = null
        }
    }
}