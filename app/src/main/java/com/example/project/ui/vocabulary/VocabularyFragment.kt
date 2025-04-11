package com.example.project.ui.vocabulary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R

class VocabularyFragment : Fragment() {

    private lateinit var vocabularyViewModel: VocabularyViewModel
    private lateinit var adapter: VocabularyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_vocabulary, container, false)

        vocabularyViewModel = ViewModelProvider(this).get(VocabularyViewModel::class.java)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recycler_vocabulary)
        recyclerView.layoutManager = LinearLayoutManager(context)

        adapter = VocabularyAdapter(emptyList()) { categoryId ->
            // Navigate to category details or directly to learning screen
            // For now, we'll pass the category to the learning screen
            val bundle = Bundle().apply {
                putString("category", categoryId)
            }
            findNavController().navigate(R.id.action_navigation_vocabulary_to_navigation_learning, bundle)
        }

        recyclerView.adapter = adapter

        vocabularyViewModel.categories.observe(viewLifecycleOwner) { categories ->
            adapter.updateData(categories)
        }

        return root
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when coming back to this screen
        vocabularyViewModel.loadCategories()
    }
}