package com.example.dvormedia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
import com.google.firebase.firestore.FirebaseFirestore

class PeopleActivity : AppCompatActivity() {

    private lateinit var personEditText: EditText
    private lateinit var addButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var peopleAdapter: PeopleAdapter
    private val peopleList = mutableListOf<Person>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: View
    private var isAdmin: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        personEditText = findViewById(R.id.person_edit_text)
        addButton = findViewById(R.id.add_button)
        recyclerView = findViewById(R.id.recyclerView)
        mainContent = findViewById(R.id.main_content) // Предполагается, что у вас есть View с id main_content

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            mainContent.setBackgroundResource(R.drawable.darkbwwb)
        } else {
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)
        }

        setupRecyclerView()
        checkUserRole()
        loadPeopleFromFirestore() // Загрузка данных из Firestore

        addButton.setOnClickListener {
            animateButtonClick(it)
            val personName = personEditText.text.toString()
            if (personName.isNotEmpty()) {
                addPerson(Person(name = personName))
            } else {
                Toast.makeText(this, "Пусто-_-", Toast.LENGTH_SHORT).show()
            }
        }
        recyclerView.addItemDecoration(
            DividerItemDecoration(this, DividerItemDecoration.VERTICAL).apply {
                setDrawable(ContextCompat.getDrawable(this@PeopleActivity, R.drawable.divider)!!)
            }
        )
    }

    private fun setupRecyclerView() {
        peopleAdapter = PeopleAdapter(peopleList, isAdmin) { person ->
            showDeleteDialog(person)
        }
        recyclerView.adapter = peopleAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
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
                        isAdmin = true
                        setupRecyclerView() // Пересоздать адаптер с правильным значением isAdmin
                    } else {
                        personEditText.visibility = View.GONE
                        addButton.visibility = View.GONE
                    }
                }
            }.addOnFailureListener {
                // Обработка ошибки
            }
        }
    }

    private fun loadPeopleFromFirestore() {
        FirebaseFirestore.getInstance().collection("people").get().addOnSuccessListener { documents ->
            if (documents != null) {
                peopleList.clear()
                for (document in documents) {
                    val person = document.toObject(Person::class.java).apply { id = document.id }
                    Log.d("PeopleActivity", "Loaded person: ${person.name}")
                    peopleList.add(person)
                }
                peopleAdapter.notifyDataSetChanged()
            } else {
                Log.d("PeopleActivity", "No documents found in 'people' collection")
            }
        }.addOnFailureListener { exception ->
            Log.e("PeopleActivity", "Error loading people from Firestore", exception)
        }
    }

    private fun addPerson(person: Person) {
        FirebaseFirestore.getInstance().collection("people").add(person).addOnSuccessListener {
            Log.d("PeopleActivity", "Person added: ${person.name}")
            peopleList.add(person)
            peopleAdapter.notifyDataSetChanged()
            personEditText.text.clear()
        }.addOnFailureListener { exception ->
            Log.e("PeopleActivity", "Error adding person to Firestore", exception)
        }
    }

    private fun showDeleteDialog(person: Person) {
        AlertDialog.Builder(this)
            .setTitle("Удалить человека")
            .setMessage("Вы действительно хотите удалить ${person.name}?")
            .setPositiveButton("Да") { _, _ ->
                deletePerson(person)
            }
            .setNegativeButton("Нет", null)
            .show()
    }

    private fun deletePerson(person: Person) {
        FirebaseFirestore.getInstance().collection("people").document(person.id).delete()
            .addOnSuccessListener {
                peopleList.remove(person)
                peopleAdapter.notifyDataSetChanged()
                Toast.makeText(this, "${person.name} удален", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка при удалении ${person.name}", Toast.LENGTH_SHORT).show()
                Log.e("PeopleActivity", "Error deleting person from Firestore", exception)
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
}