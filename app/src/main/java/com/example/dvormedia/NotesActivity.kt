package com.example.dvormedia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.SharedPreferences
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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: View
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        peopleEditText = findViewById(R.id.people_edit_text)
        saveButton = findViewById(R.id.save_button)
        recyclerView = findViewById(R.id.recyclerView)
        mainContent = findViewById(R.id.main_content) // Предполагается, что у вас есть View с id main_content

        setupRecyclerView()
        checkUserRole()
        loadNotesData() // Загрузка данных из Firestore

        saveButton.setOnClickListener {
            animateButtonClick(it)
            saveNote()
        }

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            mainContent.setBackgroundResource(R.drawable.darkbww)
        } else {
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(notesList, isAdmin) { note ->
            showDeleteDialog(note)
        }
        recyclerView.adapter = notesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@NotesActivity, R.drawable.divider)!!)
            }
        )
    }

    private fun checkUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        peopleEditText.visibility = View.VISIBLE
                        saveButton.visibility = View.VISIBLE
                        isAdmin = true
                        setupRecyclerView() // Пересоздать адаптер с правильным значением isAdmin
                    } else {
                        peopleEditText.visibility = View.GONE
                        saveButton.visibility = View.GONE
                    }
                }
            }.addOnFailureListener {
                // Обработка ошибки
            }
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
                notesList.remove(note)
                notesAdapter.notifyDataSetChanged()
                Toast.makeText(this, "Запись удалена", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener {
                Toast.makeText(this, "Ошибка при удалении записи", Toast.LENGTH_SHORT).show()
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

    override fun onDestroy() {
        super.onDestroy()
        notesListener.remove()
    }
}