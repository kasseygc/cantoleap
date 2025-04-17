package com.example.project.ui.vocabulary

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.project.R
import com.example.project.data.models.VocabularyCategory

class VocabularyAdapter(
    private var categories: List<VocabularyCategory>,
    private val onCategoryClick: (String) -> Unit
) : RecyclerView.Adapter<VocabularyAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.text_category_name)
        val descriptionTextView: TextView = view.findViewById(R.id.text_category_description)
        val progressBar: ProgressBar = view.findViewById(R.id.progress_category)
        val progressTextView: TextView = view.findViewById(R.id.text_progress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_vocabulary_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]
        holder.nameTextView.text = category.name
        holder.descriptionTextView.text = category.description
        holder.progressBar.progress = category.progress

        // Format the progress text using string resource
        val wordsCompleted = (category.progress * category.wordCount) / 100
        holder.progressTextView.text = holder.itemView.context.getString(
            R.string.words_progress_format,
            wordsCompleted,
            category.wordCount
        )

        holder.itemView.setOnClickListener {
            onCategoryClick(category.id)
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<VocabularyCategory>) {
        categories = newCategories
        notifyDataSetChanged()
    }
}