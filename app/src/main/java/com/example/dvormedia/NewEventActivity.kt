package com.example.dvormedia

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.example.dvormedia.databinding.ActivityNewEventBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

class NewEventActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewEventBinding
    private var selectedMediaUri: Uri? = null
    private var mediaType: String? = null

    companion object {
        private const val PICK_MEDIA_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewEventBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Button to pick media from gallery
        binding.uploadButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "*/*"
                putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/*", "video/*"))
            }
            startActivityForResult(Intent.createChooser(intent, "Select Picture or Video"), PICK_MEDIA_REQUEST_CODE)
        }

        // Button to save event data
        binding.saveEventButton.setOnClickListener {
            val title = binding.eventTitleEditText.text.toString().trim()
            val description = binding.eventDescEditText.text.toString().trim()
            val adminId = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

            if (selectedMediaUri != null && title.isNotEmpty() && description.isNotEmpty()) {
                val newEventRef = FirebaseFirestore.getInstance().collection("event").document()
                val eventId = newEventRef.id
                uploadMediaFile(selectedMediaUri!!, eventId) { url ->
                    val event = hashMapOf(
                        "adminId" to adminId,
                        "title" to title,
                        "description" to description,
                        "id" to eventId,
                        "imageURL" to if (mediaType == "image") url else "",
                        "videoURL" to if (mediaType == "video") url else ""
                    )
                    saveEventData(eventId, event)
                }
            } else {
                Toast.makeText(this, "Please fill all fields and select an image or video.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uploadMediaFile(fileUri: Uri, eventId: String, completion: (String) -> Unit) {
        val fileName = if (mediaType == "image") "images/$eventId" else "videos/$eventId"
        val storageRef = FirebaseStorage.getInstance().reference.child("events_media/$fileName")
        val uploadTask = storageRef.putFile(fileUri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { throw it }
            }
            storageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                completion(downloadUri.toString())
            } else {
                Toast.makeText(this, "Upload failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveEventData(eventId: String, eventData: Map<String, Any>) {
        FirebaseFirestore.getInstance().collection("event")
            .document(eventId)
            .set(eventData)
            .addOnSuccessListener {
                Toast.makeText(this, "Event saved", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save event: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_MEDIA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            data?.data?.also { uri ->
                selectedMediaUri = uri
                mediaType = contentResolver.getType(uri)?.let {
                    when {
                        it.startsWith("image/") -> "image"
                        it.startsWith("video/") -> "video"
                        else -> null
                    }
                }
                if (mediaType == "image") {
                    val imageView = findViewById<ImageView>(R.id.selectedImageView)
                    imageView.visibility = View.VISIBLE
                    findViewById<VideoView>(R.id.selectedVideoView).visibility = View.GONE
                    imageView.setImageURI(selectedMediaUri) // Отображаем изображение.
                } else if (mediaType == "video") {
                    val videoView = findViewById<VideoView>(R.id.selectedVideoView)
                    videoView.visibility = View.VISIBLE
                    findViewById<ImageView>(R.id.selectedImageView).visibility = View.GONE
                    // Устанавливаем URI видео и запускаем воспроизведение.
                    videoView.setVideoURI(selectedMediaUri)
                    videoView.start()
                }
            }
        }
    }
}