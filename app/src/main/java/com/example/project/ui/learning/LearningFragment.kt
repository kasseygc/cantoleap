package com.example.project.ui.learning

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.project.R
import com.example.project.data.models.VocabularyItem
import com.example.project.ui.notes.NotesBottomSheet

class LearningFragment : Fragment() {

    private lateinit var learningViewModel: LearningViewModel

    private lateinit var cardView: CardView
    private lateinit var chineseTextView: TextView
    private lateinit var romanizationTextView: TextView
    private lateinit var meaningTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var knowButton: Button
    private lateinit var stillLearningButton: Button
    private lateinit var viewNotesButton: Button
    private lateinit var loadingIndicator: ProgressBar
    private lateinit var errorTextView: TextView

    private var isFrontShowing = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_learning, container, false)

        // Initialize ViewModel
        learningViewModel = ViewModelProvider(this).get(LearningViewModel::class.java)

        // Initialize views
        cardView = root.findViewById(R.id.flashcard_view)
        chineseTextView = root.findViewById(R.id.text_chinese)
        romanizationTextView = root.findViewById(R.id.text_romanization)
        meaningTextView = root.findViewById(R.id.text_meaning)
        progressBar = root.findViewById(R.id.progress_learning)
        progressTextView = root.findViewById(R.id.text_progress)
        knowButton = root.findViewById(R.id.button_know)
        stillLearningButton = root.findViewById(R.id.button_still_learning)
        viewNotesButton = root.findViewById(R.id.button_view_notes)

        // Initialize other views
        loadingIndicator = root.findViewById(R.id.loading_indicator)
        errorTextView = root.findViewById(R.id.text_error)

        // Initial setup
        meaningTextView.visibility = View.GONE

        setupObservers()
        setupListeners()
        setupBackNavigation()

        // Load the category from arguments
        val categoryId = arguments?.getString("category")
        if (categoryId != null) {
            learningViewModel.loadCategory(categoryId)
        } else {
            // Default to first category if none provided
            learningViewModel.loadCategory("greetings")
        }

        return root
    }

    private fun setupBackNavigation() {
        // Handle back button press
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Navigate back to vocabulary screen
                    findNavController().navigate(R.id.action_navigation_learning_to_navigation_vocabulary)
                }
            }
        )
    }

    private fun setupObservers() {
        learningViewModel.currentWord.observe(viewLifecycleOwner) { word ->
            updateUI(word)
        }

        learningViewModel.progress.observe(viewLifecycleOwner) { progress ->
            updateProgressUI(progress)
        }

        learningViewModel.totalWords.observe(viewLifecycleOwner) { total ->
            progressBar.max = total
            updateProgressUI(learningViewModel.progress.value ?: 0)
        }

        learningViewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            loadingIndicator.visibility = if (isLoading) View.VISIBLE else View.GONE
            cardView.visibility = if (isLoading) View.GONE else View.VISIBLE
            knowButton.visibility = if (isLoading) View.GONE else View.VISIBLE
            stillLearningButton.visibility = if (isLoading) View.GONE else View.VISIBLE
            viewNotesButton.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        learningViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                errorTextView.text = error
                errorTextView.visibility = View.VISIBLE
                cardView.visibility = View.GONE
                knowButton.visibility = View.GONE
                stillLearningButton.visibility = View.GONE
                viewNotesButton.visibility = View.GONE
            } else {
                errorTextView.visibility = View.GONE
            }
        }
    }

    private fun setupListeners() {
        knowButton.setOnClickListener {
            learningViewModel.markAsKnown()
        }

        stillLearningButton.setOnClickListener {
            learningViewModel.moveToNextWord()
        }

        cardView.setOnClickListener {
            flipCard()
        }

        viewNotesButton.setOnClickListener {
            learningViewModel.currentWord.value?.let { word ->
                showNotesBottomSheet(word.id)
            }
        }
    }

    private fun updateUI(word: VocabularyItem) {
        chineseTextView.text = word.chinese
        romanizationTextView.text = word.romanization
        meaningTextView.text = word.english
    }

    private fun updateProgressUI(progress: Int) {
        progressBar.progress = progress
        progressTextView.text = getString(R.string.progress_format, progress, learningViewModel.totalWords.value ?: 0)
    }

    private fun flipCard() {
        val flipIn = AnimationUtils.loadAnimation(context, if (isFrontShowing) R.anim.flip_back_in else R.anim.flip_front_in)
        val flipOut = AnimationUtils.loadAnimation(context, if (isFrontShowing) R.anim.flip_front_out else R.anim.flip_back_out)

        flipOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                if (isFrontShowing) {
                    chineseTextView.visibility = View.GONE
                    romanizationTextView.visibility = View.GONE
                    meaningTextView.visibility = View.VISIBLE
                } else {
                    chineseTextView.visibility = View.VISIBLE
                    romanizationTextView.visibility = View.VISIBLE
                    meaningTextView.visibility = View.GONE
                }
                cardView.startAnimation(flipIn)
            }

            override fun onAnimationRepeat(animation: Animation) {}
        })

        cardView.startAnimation(flipOut)
        isFrontShowing = !isFrontShowing
    }

    private fun showNotesBottomSheet(vocabularyId: String) {
        val bottomSheet = NotesBottomSheet.newInstance(vocabularyId)
        bottomSheet.show(childFragmentManager, "notesBottomSheet")
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}