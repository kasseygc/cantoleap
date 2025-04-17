package com.example.project.ui.learning

import com.example.project.utils.AudioPlayer
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.project.R

class LearningFragment : Fragment() {

    private lateinit var learningViewModel: LearningViewModel
    private lateinit var audioPlayer: AudioPlayer
    private lateinit var loadingIndicator: ProgressBar

    private lateinit var cardView: CardView
    private lateinit var chineseTextView: TextView
    private lateinit var romanizationTextView: TextView
    private lateinit var meaningTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private lateinit var audioButton: ImageButton
    private lateinit var knowButton: Button
    private lateinit var stillLearningButton: Button

    private var isFrontShowing = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_learning, container, false)

        // Initialize AudioPlayer
        audioPlayer = AudioPlayer(requireContext())

        // Initialize views
        loadingIndicator = root.findViewById(R.id.audio_loading_indicator)
        learningViewModel = ViewModelProvider(this).get(LearningViewModel::class.java)

        // Initialize views
        cardView = root.findViewById(R.id.flashcard_view)
        chineseTextView = root.findViewById(R.id.text_chinese)
        romanizationTextView = root.findViewById(R.id.text_romanization)
        meaningTextView = root.findViewById(R.id.text_meaning)
        progressBar = root.findViewById(R.id.progress_learning)
        progressTextView = root.findViewById(R.id.text_progress)
        audioButton = root.findViewById(R.id.button_audio)
        knowButton = root.findViewById(R.id.button_know)
        stillLearningButton = root.findViewById(R.id.button_still_learning)

        // Initial setup
        meaningTextView.visibility = View.GONE

        // Setup card flip animation
        cardView.setOnClickListener {
            flipCard()
        }

        // Observe current word
        learningViewModel.currentWord.observe(viewLifecycleOwner) { word ->
            chineseTextView.text = word.chinese
            romanizationTextView.text = word.romanization
            meaningTextView.text = word.english

            // Reset card to front side
            if (!isFrontShowing) {
                flipCard()
            }
        }

        // Observe progress
        learningViewModel.progress.observe(viewLifecycleOwner) { progress ->
            progressBar.progress = progress
        }

        learningViewModel.totalWords.observe(viewLifecycleOwner) { total ->
            progressBar.max = total
            updateProgressText()
        }

        // Setup audio button
        audioButton.setOnClickListener {
            learningViewModel.currentWord.value?.let { word ->
                loadingIndicator.visibility = View.VISIBLE
                audioPlayer.playAudio(
                    audioRef = word.audioRef,
                    onStarted = {
                        requireActivity().runOnUiThread {
                            loadingIndicator.visibility = View.GONE
                        }
                    },
                    onError = {
                        requireActivity().runOnUiThread {
                            loadingIndicator.visibility = View.GONE
                            Toast.makeText(context, getString(R.string.audio_load_error), Toast.LENGTH_SHORT).show()
                        }
                    },
                    onCompleted = {
                        // Audio playback finished
                    }
                )
            }
        }

        // Setup buttons
        knowButton.setOnClickListener {
            learningViewModel.markAsKnown()
            learningViewModel.moveToNextWord()
        }

        stillLearningButton.setOnClickListener {
            learningViewModel.moveToNextWord()
        }

        // Get category from arguments if available
        arguments?.getString("category")?.let { category ->
            learningViewModel.loadCategory(category)
        }

        return root
    }

    private fun updateProgressText() {
        val progress = learningViewModel.progress.value ?: 0
        val total = learningViewModel.totalWords.value ?: 0
        progressTextView.text = getString(R.string.progress_format, progress, total)
    }

    private fun flipCard() {
        // Load animations
        val frontAnimation = AnimationUtils.loadAnimation(context,
            if (isFrontShowing) R.anim.flip_front_out else R.anim.flip_front_in)
        val backAnimation = AnimationUtils.loadAnimation(context,
            if (isFrontShowing) R.anim.flip_back_in else R.anim.flip_back_out)

        frontAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Toggle visibility when front animation ends
                if (isFrontShowing) {
                    meaningTextView.visibility = View.VISIBLE
                    romanizationTextView.visibility = View.GONE
                } else {
                    meaningTextView.visibility = View.GONE
                    romanizationTextView.visibility = View.VISIBLE
                }

                // Start back animation
                cardView.startAnimation(backAnimation)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        backAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}

            override fun onAnimationEnd(animation: Animation?) {
                // Update state after animation completes
                isFrontShowing = !isFrontShowing
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })

        // Start front animation
        cardView.startAnimation(frontAnimation)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayer.releaseMediaPlayer()
    }
}