package com.example.dvormedia

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PeopleActivity : AppCompatActivity() {

    private lateinit var personEditText: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleAdapter
    private val peopleList = mutableListOf<Person>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        personEditText = findViewById(R.id.person_edit_text)
        addButton = findViewById(R.id.add_button)
        recyclerView = findViewById(R.id.recyclerView)

        setupRecyclerView()
        checkUserRole()

        addButton.setOnClickListener {
            val personName = personEditText.text.toString()
            if (personName.isNotEmpty()) {
                addPerson(Person(name = personName))
            }
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@PeopleActivity, R.drawable.divider)!!)
            }
        )
    }

    private fun setupRecyclerView() {
        peopleAdapter = PeopleAdapter(peopleList) { person ->
            // Действие при долгом нажатии на элемент
        }
        recyclerView.adapter = peopleAdapter
    }

    private fun checkUserRole() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        personEditText.visibility = View.VISIBLE
                        addButton.visibility = View.VISIBLE
                    }
                }
            }.addOnFailureListener {
                // Обработка ошибки
            }
        }
    }

    private fun addPerson(person: Person) {
        // Логика добавления человека в список
        peopleList.add(person)
        peopleAdapter.notifyDataSetChanged()
        personEditText.text.clear()
    }
}