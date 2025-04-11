package com.example.project.data.models

data class QuizQuestion(
    val prompt: String,
    val options: List<String>,
    val correctAnswerIndex: Int,
    val vocabularyItemId: String
)
