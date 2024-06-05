package com.example.dvormedia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class NewEventActivity : AppCompatActivity() {

    private lateinit var eventTitleEditText: EditText
    private lateinit var eventDescEditText: EditText
    private lateinit var saveEventButton: Button
    private lateinit var uploadButton: Button
    private lateinit var selectedImageView: ImageView
    private lateinit var selectedVideoView: VideoView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: View

    private val PICK_IMAGE_VIDEO_REQUEST = 1
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    private var selectedMediaUri: Uri? = null
    private var isImage: Boolean = false

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
            openGalleryForImageAndVideo()
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

        // Сначала загружаем медиафайл, если он выбран
        if (selectedMediaUri != null) {
            uploadMediaToStorage(title, description)
        } else {
            saveEventToFirestore(title, description, "", "")
        }
    }

    private fun uploadMediaToStorage(title: String, description: String) {
        val storageRef: StorageReference = if (isImage) {
            storage.reference.child("events_media/images/${System.currentTimeMillis()}.jpg")
        } else {
            storage.reference.child("events_media/videos/${System.currentTimeMillis()}.mp4")
        }

        selectedMediaUri?.let { uri ->
            storageRef.putFile(uri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        if (isImage) {
                            saveEventToFirestore(title, description, downloadUrl.toString(), "")
                        } else {
                            saveEventToFirestore(title, description, "", downloadUrl.toString())
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Ошибка при загрузке медиафайла: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveEventToFirestore(title: String, description: String, imageUrl: String, videoUrl: String) {
        val adminId = "someAdminId" // Замените на реальный adminId
        val id = db.collection("event").document().id
        val event = Event(
            adminId = adminId,
            description = description,
            id = id,
            imageURL = imageUrl,
            title = title,
            videoURL = videoUrl
        )

        db.collection("event")
            .document(id)
            .set(event)
            .addOnSuccessListener {
                Toast.makeText(this, "Мероприятие сохранено", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Ошибка при сохранении мероприятия: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

    private fun openGalleryForImageAndVideo() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/* video/*"
        startActivityForResult(intent, PICK_IMAGE_VIDEO_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_VIDEO_REQUEST && resultCode == RESULT_OK) {
            selectedMediaUri = data?.data
            selectedMediaUri?.let { uri ->
                val mimeType = contentResolver.getType(uri)
                if (mimeType != null) {
                    if (mimeType.startsWith("image")) {
                        selectedImageView.setImageURI(uri)
                        selectedImageView.visibility = View.VISIBLE
                        selectedVideoView.visibility = View.GONE
                        isImage = true
                    } else if (mimeType.startsWith("video")) {
                        selectedVideoView.setVideoURI(uri)
                        selectedVideoView.visibility = View.VISIBLE
                        selectedImageView.visibility = View.GONE
                        selectedVideoView.start()
                        isImage = false
                    }
                }
            }
        }
    }
}