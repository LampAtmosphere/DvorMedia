package com.example.dvormedia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class PeopleActivity : AppCompatActivity() {

    private lateinit var personEditText: EditText
    private lateinit var addButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        personEditText = findViewById(R.id.person_edit_text)
        addButton = findViewById(R.id.add_button)

        addButton.setOnClickListener {
            addPerson()
        }
    }

    private fun addPerson() {
        val personName = personEditText.text.toString()
        val person = hashMapOf(
            "name" to personName
        )

        FirebaseFirestore.getInstance().collection("people").add(person).addOnSuccessListener {
            // Успешно добавлено
        }.addOnFailureListener {
            // Обработка ошибки
        }
    }
}