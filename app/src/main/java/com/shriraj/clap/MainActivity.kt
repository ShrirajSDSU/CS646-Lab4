package com.shriraj.clap
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.workDataOf


class MainActivity : AppCompatActivity() {
    // Variables for tracking the user's score and the current question
    private var points = 0
    private var currentQuestion: String? = null
    private var currentAnswer: String? = null

    // Sound Effect Variables
    private var totalQuestion = 0
    private var soundPool: SoundPool? = null
    private var successSound = -1
    private var failureSound = -1

    // Views variables
    private var imgClap: ImageView? = null
    private var textView1: TextView? = null
    private var response: EditText? = null

    // Main function that is called after the activity is created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        imgClap = findViewById(R.id.imgClap)
        textView1 = findViewById(R.id.textView1)
        response = findViewById(R.id.response_edit_text)
        initialize()
    }

    // Function that sets the game up
    fun initialize() {
        assignQuestion()
        resetView()
        initializeSound()
    }

    // Function that initializes the sound
    fun initializeSound() {
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(attributes)
            .build()

        successSound =  soundPool!!.load(baseContext, R.raw.clap, 1)
        failureSound =  soundPool!!.load(baseContext, R.raw.wrong, 1)

    }

    // Function that assigns the question
    fun assignQuestion() {
        // Define a list of questions and answers
        val questions = listOf(
            "Which animal is known as the \n'Ship of the Desert\"?",
            "Rainbow consist of how many colours?",
            "How many vowels are there in \nthe English alphabet?",
            "Which animal is known as the \nking of the jungle?",
            "Which festival is known as the\n festival of light?",
            "Which is the tallest animal\n on the earth?",
            "How many stars and stripes are\n on the US flag?"
        )
        val answers = listOf(
            "Camel",
            "7",
            "5",
            "Lion",
            "Diwali",
            "Giraffe",
            "50"
        )

        // Randomly select a question and its corresponding answer
        val index = (0 until questions.size).shuffled().first()
        currentQuestion = questions[index]
        currentAnswer = answers[index]
    }


    // Function that calculates the click
    fun calculateClick(view: View) {
        val response = response?.text.toString()

        if(TextUtils.isEmpty(response)) {
            return;
        }

        if(response == currentAnswer)
            correctResult()
        else
            wrongAnswer()
    }

    // Function that resets the view
    fun resetView() {
        response?.setText("")
        textView1?.setText(currentQuestion)
    }

    // Function that calculates the correct result
    fun correctResult() {
        points++
        totalQuestion++

        //animate
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.animate)
        imgClap?.setBackgroundResource(R.drawable.clap);
        imgClap?.startAnimation(anim)

        //play sound
        soundPool?.play(successSound, 1F, 1F, 0, 0, 1F)

        assignQuestion()
        resetView()
    }

    // Function that calculates the wrong answer
    fun wrongAnswer() {
        assignQuestion()
        resetView()
        totalQuestion++

        soundPool?.play(failureSound, 1F, 1F, 0, 0, 1F)

        //animate
        val anim: Animation = AnimationUtils.loadAnimation(this, R.anim.animate)
        imgClap?.setBackgroundResource(R.drawable.tryagain);
        imgClap?.startAnimation(anim)

    }

    // Function that is called after the activity is stopped
    override fun onStop() {
        super.onStop()

        val message = "your score " + points + " out of " + totalQuestion
        // Start the Worker if the timer is running
        val timerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<TimerWorker>()
            .setInputData(
                workDataOf(
                    KEY_SCORE to message
                )
            ).build()

        WorkManager.getInstance(applicationContext).enqueue(timerWorkRequest)
    }
}