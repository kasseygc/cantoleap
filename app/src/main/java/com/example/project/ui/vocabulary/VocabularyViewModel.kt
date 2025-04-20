package com.example.project.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.repository.DataRepository
import com.example.project.data.repository.ProgressRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application)
    private val progressRepository = ProgressRepository(application)

    private val _categories = MutableLiveData<List<VocabularyCategory>>()
    val categories: LiveData<List<VocabularyCategory>> = _categories

    private val _loading = MutableLiveData<Boolean>(true)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>(null)
    val error: LiveData<String?> = _error

    // Search functionality
    private val _searchQuery = MutableLiveData<String>("")
    val searchQuery: LiveData<String> = _searchQuery

    private val _filteredCategories = MutableLiveData<List<VocabularyCategory>>()
    val filteredCategories: LiveData<List<VocabularyCategory>> = _filteredCategories

    // User ID (replace with actual user authentication logic)
    private val userId = "defaultUser"

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.postValue(true)
                val categoryList = repository.getCategories()

                // Get progress for each category
                val updatedList = updateCategoriesWithProgress(categoryList)

                _categories.postValue(updatedList)
                _filteredCategories.postValue(updatedList)
                _error.postValue(null)
            } catch (e: Exception) {
                _error.postValue("Failed to load categories: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }

    private suspend fun updateCategoriesWithProgress(categories: List<VocabularyCategory>): List<VocabularyCategory> {
        // Get user progress
        val progressMap = progressRepository.getUserProgress(userId).value ?: emptyMap()

        return categories.map { category ->
            // Get words for this category
            val words = repository.getVocabularyByCategory(category.id)

            // Calculate progress percentage
            val learnedCount = words.count { progressMap[it.id] ?: 0 > 0 }
            val progressPercent = if (words.isEmpty()) 0 else (learnedCount * 100 / words.size)

            // Create updated category with progress
            category.copy(progress = progressPercent)
        }
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
        filterCategories()
    }

    private fun filterCategories() {
        val query = _searchQuery.value ?: ""
        val allCategories = _categories.value ?: return

        if (query.isEmpty()) {
            _filteredCategories.value = allCategories
            return
        }

        val filtered = allCategories.filter {
            it.name.contains(query, ignoreCase = true) ||
                    it.description.contains(query, ignoreCase = true)
        }
        _filteredCategories.value = filtered
    }
}