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

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = DataRepository(application)
    private val progressRepository = ProgressRepository(application)

    // User ID (replace with actual user authentication logic)
    private val userId = "defaultUser"

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

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            try {
                _loading.value = true

                // Get categories from repository
                val categoryList = repository.getCategories()

                // Get progress on main thread
                val progress = progressRepository.getUserProgress(userId)

                // Observe progress and update categories when it changes
                progress.observeForever { progressMap ->
                    // Update progress for each category (still on main thread)
                    val updatedCategories = updateCategoriesWithProgress(categoryList, progressMap)
                    _categories.value = updatedCategories
                    _filteredCategories.value = updatedCategories

                    // Only observe once, then remove
                    progress.removeObserver { }
                }

                _error.value = null
            } catch (e: Exception) {
                _error.value = "Failed to load categories: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun updateCategoriesWithProgress(
        categories: List<VocabularyCategory>,
        progressMap: Map<String, Int>
    ): List<VocabularyCategory> {
        // Run this on main thread
        return categories.map { category ->
            val words = repository.getVocabularyByCategory(category.id)
            val learnedCount = words.count { progressMap[it.id] ?: 0 > 0 }
            val progressPercent = if (words.isEmpty()) 0 else (learnedCount * 100 / words.size)

            // Create a new category with the updated progress
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