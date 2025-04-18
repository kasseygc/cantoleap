package com.example.project.data.models

data class VocabularyItem(
    val id: String,
    val category: String,
    val chinese: String,
    val romanization: String,
    val english: String,
    val difficulty: Int = 1
)