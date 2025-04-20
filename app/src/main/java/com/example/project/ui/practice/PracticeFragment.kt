package com.example.project.ui.practice

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.project.R

class PracticeFragment : Fragment() {

    private lateinit var practiceViewModel: PracticeViewModel

    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var questionChineseTextView: TextView
    private lateinit var questionRomanizationTextView: TextView
    private lateinit var questionCard: CardView

    private lateinit var optionCards: List<CardView>
    private lateinit var optionTexts: List<TextView>
    private lateinit var submitButton: Button

    // Quiz completion views
    private lateinit var quizCompletedLayout: LinearLayout
    private lateinit var scoreSummaryTextView: TextView
    private lateinit var tryAgainButton: Button
    private lateinit var backToCategoriesButton: Button

    private var selectedOptionIndex: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_practice, container, false)

        // Initialize ViewModel
        practiceViewModel = ViewModelProvider(this).get(PracticeViewModel::class.java)

        // Initialize views
        progressBar = root.findViewById(R.id.progress_quiz)
        progressTextView = root.findViewById(R.id.text_quiz_progress)
        questionChineseTextView = root.findViewById(R.id.text_question_chinese)
        questionRomanizationTextView = root.findViewById(R.id.text_question_romanization)
        questionCard = root.findViewById(R.id.card_question)
        submitButton = root.findViewById(R.id.button_submit)

        // Initialize quiz completion views
        quizCompletedLayout = root.findViewById(R.id.layout_quiz_complete)
        scoreSummaryTextView = root.findViewById(R.id.text_score_summary)
        tryAgainButton = root.findViewById(R.id.button_try_again)
        backToCategoriesButton = root.findViewById(R.id.button_back_to_categories)

        // Initialize option cards and texts
        optionCards = listOf(
            root.findViewById(R.id.card_option_1),
            root.findViewById(R.id.card_option_2),
            root.findViewById(R.id.card_option_3),
            root.findViewById(R.id.card_option_4)
        )

        optionTexts = listOf(
            root.findViewById(R.id.text_option_1),
            root.findViewById(R.id.text_option_2),
            root.findViewById(R.id.text_option_3),
            root.findViewById(R.id.text_option_4)
        )

        // Set up option selection
        for (i in optionCards.indices) {
            optionCards[i].setOnClickListener {
                selectOption(i)
            }
        }

        setupObservers()
        setupClickListeners()

        // Get category from arguments and start quiz
        arguments?.getString("category")?.let { category ->
            practiceViewModel.startQuiz(category)
        } ?: run {
            // If no category provided, start a mixed quiz
            practiceViewModel.startMixedQuiz()
        }

        return root
    }

    private fun setupObservers() {
        // Observe current question
        practiceViewModel.currentQuestion.observe(viewLifecycleOwner) { question ->
            // Reset selection
            selectedOptionIndex = null
            resetOptionCardStyles()

            // Update question UI
            questionChineseTextView.text = question.prompt

            // Fetch vocabulary item details for this question
            practiceViewModel.getVocabularyDetails(question.vocabularyItemId) { item ->
                if (item != null) {
                    questionRomanizationTextView.text = item.romanization
                }
            }

            // Set up options
            for (i in question.options.indices) {
                if (i < optionTexts.size) {
                    optionTexts[i].text = question.options[i]
                }
            }
        }

        // Observe quiz progress
        practiceViewModel.currentQuestionIndex.observe(viewLifecycleOwner) { index ->
            updateProgressUI()
        }

        practiceViewModel.totalQuestions.observe(viewLifecycleOwner) { total ->
            progressBar.max = total
            updateProgressUI()
        }

        // Observe quiz completion
        practiceViewModel.isQuizComplete.observe(viewLifecycleOwner) { isComplete ->
            if (isComplete) {
                showQuizCompletedUI()
            } else {
                hideQuizCompletedUI()
            }
        }

        // Observe score updates
        practiceViewModel.score.observe(viewLifecycleOwner) { score ->
            val total = practiceViewModel.totalQuestions.value ?: 0
            scoreSummaryTextView.text = getString(R.string.score_format, score, total)
        }
    }

    private fun setupClickListeners() {
        submitButton.setOnClickListener {
            if (selectedOptionIndex != null) {
                practiceViewModel.submitAnswer(selectedOptionIndex!!)

                // Show correct/incorrect feedback
                showAnswerFeedback()
            } else {
                Toast.makeText(context, getString(R.string.select_an_answer), Toast.LENGTH_SHORT).show()
            }
        }

        tryAgainButton.setOnClickListener {
            // Restart the current quiz
            arguments?.getString("category")?.let { category ->
                practiceViewModel.startQuiz(category)
            } ?: run {
                practiceViewModel.startMixedQuiz()
            }
        }

        backToCategoriesButton.setOnClickListener {
            // Navigate back to vocabulary categories
            findNavController().navigate(R.id.navigation_vocabulary)
        }
    }


    private fun updateProgressUI() {
        val current = practiceViewModel.currentQuestionIndex.value ?: 0
        val total = practiceViewModel.totalQuestions.value ?: 0

        // Use string resource with formatting
        progressTextView.text = getString(R.string.progress_format, current + 1, total)

        // Update progress bar
        progressBar.progress = current + 1
    }

    private fun selectOption(index: Int) {
        // Deselect previous option
        selectedOptionIndex?.let { prevIndex ->
            optionCards[prevIndex].setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }

        // Select new option
        selectedOptionIndex = index
        optionCards[index].setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.option_selected)
        )
    }

    private fun resetOptionCardStyles() {
        for (card in optionCards) {
            card.setCardBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.white)
            )
        }
    }

    private fun showAnswerFeedback() {
        val currentQuestion = practiceViewModel.currentQuestion.value ?: return
        val correctIndex = currentQuestion.correctAnswerIndex

        // Show correct answer in green
        optionCards[correctIndex].setCardBackgroundColor(
            ContextCompat.getColor(requireContext(), R.color.option_correct)
        )

        // If selected answer is wrong, show it in red
        selectedOptionIndex?.let { selected ->
            if (selected != correctIndex) {
                optionCards[selected].setCardBackgroundColor(
                    ContextCompat.getColor(requireContext(), R.color.option_incorrect)
                )
            }
        }

        // Disable option selection
        for (card in optionCards) {
            card.isClickable = false
        }

        // Change submit button to "Next"
        submitButton.text = getString(R.string.next)
        submitButton.setOnClickListener {
            practiceViewModel.moveToNextQuestion()

            // Reset UI for next question
            resetOptionCardStyles()
            for (card in optionCards) {
                card.isClickable = true
            }

            // Reset submit button
            submitButton.text = getString(R.string.submit)
            submitButton.setOnClickListener {
                if (selectedOptionIndex != null) {
                    practiceViewModel.submitAnswer(selectedOptionIndex!!)
                    showAnswerFeedback()
                } else {
                    Toast.makeText(context, getString(R.string.select_an_answer), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showQuizCompletedUI() {
        // Hide question UI
        questionCard.visibility = View.GONE
        for (card in optionCards) {
            card.visibility = View.GONE
        }
        submitButton.visibility = View.GONE

        // Show completion UI
        quizCompletedLayout.visibility = View.VISIBLE

        // Update score summary
        val score = practiceViewModel.score.value ?: 0
        val total = practiceViewModel.totalQuestions.value ?: 0
        scoreSummaryTextView.text = getString(R.string.score_format, score, total)
    }

    private fun hideQuizCompletedUI() {
        // Show question UI
        questionCard.visibility = View.VISIBLE
        for (card in optionCards) {
            card.visibility = View.VISIBLE
        }
        submitButton.visibility = View.VISIBLE

        // Hide completion UI
        quizCompletedLayout.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }
}