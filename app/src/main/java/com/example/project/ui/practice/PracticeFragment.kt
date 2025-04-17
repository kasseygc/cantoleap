package com.example.project.ui.practice

import com.example.project.utils.AudioPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.project.R

class PracticeFragment : Fragment() {

    private lateinit var practiceViewModel: PracticeViewModel
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var questionChineseTextView: TextView
    private lateinit var questionRomanizationTextView: TextView
    private lateinit var questionAudioButton: ImageButton
    private lateinit var optionCards: List<CardView>
    private lateinit var optionTexts: List<TextView>
    private lateinit var submitButton: Button

    private var selectedOptionIndex: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_practice, container, false)

        practiceViewModel = ViewModelProvider(this).get(PracticeViewModel::class.java)

        // Initialize AudioPlayer
        audioPlayer = AudioPlayer(requireContext())

        // Initialize views
        loadingIndicator = root.findViewById(R.id.audio_loading_indicator)
        progressBar = root.findViewById(R.id.progress_quiz)
        progressTextView = root.findViewById(R.id.text_quiz_progress)
        questionChineseTextView = root.findViewById(R.id.text_question_chinese)
        questionRomanizationTextView = root.findViewById(R.id.text_question_romanization)
        questionAudioButton = root.findViewById(R.id.button_question_audio)
        submitButton = root.findViewById(R.id.button_submit)

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

                    // Set up audio button
                    questionAudioButton.setOnClickListener {
                        loadingIndicator.visibility = View.VISIBLE
                        audioPlayer.playAudio(
                            audioRef = item.audioRef,
                            onStarted = {
                                requireActivity().runOnUiThread {
                                    loadingIndicator.visibility = View.GONE
                                }
                            },
                            onError = {
                                requireActivity().runOnUiThread {
                                    loadingIndicator.visibility = View.GONE
                                    Toast.makeText(
                                        context,
                                        getString(R.string.audio_load_error),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            onCompleted = {
                                // Audio playback finished
                            }
                        )
                    }
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

        // Submit button
        submitButton.setOnClickListener {
            if (selectedOptionIndex != null) {
                practiceViewModel.submitAnswer(selectedOptionIndex!!)

                // Show correct/incorrect feedback
                showAnswerFeedback()
            } else {
                Toast.makeText(context, getString(R.string.select_an_answer), Toast.LENGTH_SHORT).show()
            }
        }

        // Get category from arguments and start quiz
        arguments?.getString("category")?.let { category ->
            practiceViewModel.startQuiz(category)
        } ?: run {
            // If no category provided, start a mixed quiz
            practiceViewModel.startMixedQuiz()
        }

        return root
    }

    private fun updateProgressUI() {
        val current = practiceViewModel.currentQuestionIndex.value ?: 0
        val total = practiceViewModel.totalQuestions.value ?: 0

        progressBar.progress = current + 1
        progressTextView.text = getString(R.string.progress_format, current + 1, total)
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

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayer.releaseMediaPlayer()
    }
}