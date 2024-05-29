package com.example.dvormedia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class NewEventActivity : AppCompatActivity() {

    private lateinit var eventTitleEditText: EditText
    private lateinit var eventDescEditText: EditText
    private lateinit var saveEventButton: Button
    private lateinit var uploadButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var selectedVideoView: VideoView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_event)

        eventTitleEditText = findViewById(R.id.eventTitleEditText)
        eventDescEditText = findViewById(R.id.eventDescEditText)
        saveEventButton = findViewById(R.id.saveEventButton)
        uploadButton = findViewById(R.id.uploadButton)
        selectedImageView = findViewById(R.id.selectedImageView)
        selectedVideoView = findViewById(R.id.selectedVideoView)
        mainContent = findViewById(R.id.main_content) // Предполагается, что у вас есть View с id main_content

        saveEventButton.setOnClickListener {
            animateButtonClick(it)
            saveEvent()
        }

        uploadButton.setOnClickListener {
            animateButtonClick(it)
            // Логика загрузки изображений или видео
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            mainContent.setBackgroundResource(R.drawable.darkbbwb)
        } else {
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)
        }
    }

    private fun saveEvent() {
        val title = eventTitleEditText.text.toString()
        val description = eventDescEditText.text.toString()

        if (title.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        // Логика сохранения мероприятия в базу данных
    }

    private fun animateButtonClick(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f, 1f)
        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f, 1f)
        scaleX.duration = 100
        scaleY.duration = 100

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(scaleX, scaleY)
        animatorSet.start()
    }
}