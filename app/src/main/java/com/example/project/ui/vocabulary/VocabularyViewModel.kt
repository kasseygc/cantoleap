package com.example.project.ui.vocabulary

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.project.data.models.VocabularyCategory

class VocabularyViewModel : ViewModel() {

    private val _categories = MutableLiveData<List<VocabularyCategory>>().apply {
        // For now, populate with sample data
        value = getSampleCategories()
    }

    val categories: LiveData<List<VocabularyCategory>> = _categories

    // Sample data for testing - will be replaced with database calls
    private fun getSampleCategories(): List<VocabularyCategory> {
        return listOf(
            VocabularyCategory("1", "Greetings", "Basic greetings and introductions", 20, 5),
            VocabularyCategory("2", "Food", "Common food items and restaurant phrases", 30, 0),
            VocabularyCategory("3", "Numbers", "Numbers and counting", 15, 10),
            VocabularyCategory("4", "Family", "Family members and relationships", 25, 0),
            VocabularyCategory("5", "Travel", "Transportation and directions", 25, 0)
        )
    }
}