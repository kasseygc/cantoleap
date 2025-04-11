package com.example.project.data.models

data class VocabularyCategory(
    val id: String,
    val name: String,
    val description: String,
    val wordCount: Int,
    val progress: Int = 0  // Percentage complete
)
