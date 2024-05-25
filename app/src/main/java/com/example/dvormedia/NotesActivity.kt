package com.example.dvormedia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class NotesActivity : AppCompatActivity() {

    private lateinit var peopleEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        peopleEditText = findViewById(R.id.people_edit_text)
        saveButton = findViewById(R.id.save_button)

        saveButton.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        val people = peopleEditText.text.toString().split(",").map { it.trim() }
        val note = hashMapOf(
            "date" to SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()),
            "people" to people
        )

        FirebaseFirestore.getInstance().collection("notes").add(note).addOnSuccessListener {
            // Успешно сохранено
        }.addOnFailureListener {
            // Обработка ошибки
        }
    }
}