package com.example.project.ui.vocabulary

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.project.data.models.VocabularyCategory
import com.example.project.data.repository.VocabularyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class VocabularyViewModel(application: Application) : AndroidViewModel(application) {

    private val _categories = MutableLiveData<List<VocabularyCategory>>()
    val categories: LiveData<List<VocabularyCategory>> = _categories

    private val repository = VocabularyRepository(application)

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch(Dispatchers.IO) {
            val categoryList = repository.getCategories()
            _categories.postValue(categoryList)
        }
    }
}