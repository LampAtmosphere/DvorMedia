package com.example.dvormedia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Note(
    val date: String = "",
    var people: List<String> = emptyList(),
    val id: String = ""
)

class NotesActivity : AppCompatActivity() {

    private lateinit var peopleEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var notesAdapter: NotesAdapter
    private val notesList = mutableListOf<Note>()
    private lateinit var notesListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        peopleEditText = findViewById(R.id.people_edit_text)
        saveButton = findViewById(R.id.save_button)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        notesAdapter = NotesAdapter(notesList) { note ->
            showDeleteDialog(note)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@NotesActivity, R.drawable.divider)!!)
            }
        )
        }
        recyclerView.adapter = notesAdapter

        // Проверка роли пользователя
        checkUserRole { isAdmin ->
            if (isAdmin) {
                saveButton.setOnClickListener {
                    saveNote()
                }
                peopleEditText.visibility = View.VISIBLE
                saveButton.visibility = View.VISIBLE
            } else {
                peopleEditText.visibility = View.GONE
                saveButton.visibility = View.GONE
            }
        }

        loadNotesData()
    }

    private fun saveNote() {
        val people = peopleEditText.text.toString().split(",").map { it.trim() }.filter { it.isNotEmpty() }
        if (people.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите хотя бы одно имя", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())

        val noteRef = FirebaseFirestore.getInstance().collection("notes").document(currentDate)
        noteRef.get().addOnSuccessListener { document ->
            if (document.exists()) {
                noteRef.update("people", FieldValue.arrayUnion(*people.toTypedArray()))
            } else {
                val note = hashMapOf(
                    "date" to currentDate,
                    "people" to people
                )
                noteRef.set(note)
            }
        }.addOnCompleteListener {
            // Показать сообщение о сохранении
            Toast.makeText(this, "Данные сохранены", Toast.LENGTH_SHORT).show()
            // Очистить поле ввода
            peopleEditText.text.clear()
        }
    }

    private fun loadNotesData() {
        notesListener = FirebaseFirestore.getInstance().collection("notes")
            .addSnapshotListener { documents, error ->
                if (error == null && documents != null) {
                    notesList.clear()
                    for (document in documents) {
                        try {
                            val note = document.toObject(Note::class.java).copy(id = document.id)
                            notesList.add(note)
                        } catch (e: Exception) {
                            // Обработка старого формата данных
                            val date = document.getString("date") ?: ""
                            val people = document.get("people")
                            val peopleList = if (people is String) {
                                people.split(",").map { it.trim() }
                            } else {
                                people as? List<String> ?: emptyList()
                            }
                            val note = Note(
                                date = date,
                                people = peopleList,
                                id = document.id
                            )
                            notesList.add(note)

                            // Обновление формата данных в Firestore
                            FirebaseFirestore.getInstance().collection("notes")
                                .document(document.id)
                                .update("people", peopleList)
                        }
                    }
                    notesAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showDeleteDialog(note: Note) {
        AlertDialog.Builder(this)
            .setTitle("Удалить запись")
            .setMessage("Вы хотите удалить эту запись?")
            .setPositiveButton("Да") { _, _ ->
                deleteNote(note)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deleteNote(note: Note) {
        FirebaseFirestore.getInstance().collection("notes").document(note.id).delete()
            .addOnSuccessListener {
                // Успешно удалено
            }.addOnFailureListener {
                // Обработка ошибки
            }
    }

    private fun checkUserRole(callback: (Boolean) -> Unit) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    callback(role == "admin")
                } else {
                    callback(false)
                }
            }.addOnFailureListener {
                callback(false)
            }
        } else {
            callback(false)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        notesListener.remove()
    }
}