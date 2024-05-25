package com.example.dvormedia

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class PeopleActivity : AppCompatActivity() {

    private lateinit var personEditText: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleAdapter
    private val peopleList = mutableListOf<Person>()
    private lateinit var peopleListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        personEditText = findViewById(R.id.person_edit_text)
        addButton = findViewById(R.id.add_button)
        recyclerView = findViewById(R.id.recyclerView)

        recyclerView.layoutManager = LinearLayoutManager(this)
        peopleAdapter = PeopleAdapter(peopleList) { person ->
            showDeleteDialog(person)
        }
        recyclerView.adapter = peopleAdapter

        // Проверка роли пользователя
        checkUserRole { isAdmin ->
            if (isAdmin) {
                addButton.setOnClickListener {
                    addPerson()
                }
            } else {
                addButton.isEnabled = false
                personEditText.isEnabled = false
                Toast.makeText(this, "Доступ запрещен", Toast.LENGTH_SHORT).show()
            }
        }

        loadPeopleData()
    }

    private fun addPerson() {
        val personName = personEditText.text.toString().trim()
        if (personName.isEmpty()) {
            Toast.makeText(this, "Пожалуйста, введите имя", Toast.LENGTH_SHORT).show()
            return
        }

        val person = hashMapOf(
            "name" to personName
        )

        FirebaseFirestore.getInstance().collection("people").add(person).addOnSuccessListener {
            // Успешно добавлено
        }.addOnFailureListener {
            // Обработка ошибки
        }
    }

    private fun loadPeopleData() {
        peopleListener = FirebaseFirestore.getInstance().collection("people")
            .addSnapshotListener { documents, error ->
                if (error == null && documents != null) {
                    peopleList.clear()
                    for (document in documents) {
                        val person = document.toObject(Person::class.java).copy(id = document.id)
                        peopleList.add(person)
                    }
                    peopleAdapter.notifyDataSetChanged()
                }
            }
    }

    private fun showDeleteDialog(person: Person) {
        AlertDialog.Builder(this)
            .setTitle("Удалить человека")
            .setMessage("Вы хотите удалить этого человека?")
            .setPositiveButton("Да") { _, _ ->
                deletePerson(person)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deletePerson(person: Person) {
        FirebaseFirestore.getInstance().collection("people").document(person.id).delete()
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
        peopleListener.remove()
    }
}