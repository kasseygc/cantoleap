package com.example.project.ui.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.ui.notes.NotesBottomSheet

class VocabularyFragment : Fragment() {
    private lateinit var vocabularyViewModel: VocabularyViewModel

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var errorTextView: TextView
    private lateinit var searchEditText: EditText
    private lateinit var searchButton: ImageButton
    private lateinit var adapter: VocabularyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_vocabulary, container, false)

        // Initialize views
        recyclerView = root.findViewById(R.id.recycler_vocabulary)
        progressBar = root.findViewById(R.id.progress_loading)
        errorTextView = root.findViewById(R.id.text_error)
        searchEditText = root.findViewById(R.id.edit_search)
        searchButton = root.findViewById(R.id.button_search)

        // Initialize ViewModel
        vocabularyViewModel = ViewModelProvider(this).get(VocabularyViewModel::class.java)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        adapter = VocabularyAdapter(emptyList()) { categoryId ->
            // Navigate to Learning screen with selected category
            val bundle = Bundle().apply {
                putString("category", categoryId)
            }
            findNavController().navigate(R.id.action_navigation_vocabulary_to_navigation_learning, bundle)
        }
        recyclerView.adapter = adapter

        setupObservers()
        setupListeners()

        return root
    }

    private fun setupObservers() {
        // Observe filtered categories
        vocabularyViewModel.filteredCategories.observe(viewLifecycleOwner) { categories ->
            adapter.updateData(categories)

            // Show/hide empty state
            if (categories.isEmpty()) {
                errorTextView.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                errorTextView.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }

        // Observe loading state
        vocabularyViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        vocabularyViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                errorTextView.text = error
                errorTextView.visibility = View.VISIBLE
            } else {
                errorTextView.visibility = View.GONE
            }
        }

        // Observe search query
        vocabularyViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            searchEditText.setText(query)
        }
    }

    private fun setupListeners() {
        searchButton.setOnClickListener {
            val query = searchEditText.text.toString().trim()
            vocabularyViewModel.updateSearch(query)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to this screen
        vocabularyViewModel.loadCategories()
    }

    // For showing notes from external fragments
    fun showNotesBottomSheet(vocabularyId: String) {
        val bottomSheet = NotesBottomSheet.newInstance(vocabularyId)
        bottomSheet.show(childFragmentManager, "notesBottomSheet")
    }
}