package com.example.dvormedia

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExampleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private val events = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        recyclerView = findViewById(R.id.recyclerView)
        mainContent = findViewById(R.id.main_content)

        adapter = EventsAdapter(events)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExampleActivity)
            adapter = this@ExampleActivity.adapter
        }

        fetchEvents()

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            mainContent.setBackgroundResource(R.drawable.darkbww)
        } else {
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)
        }
    }

    private fun fetchEvents() {
        db.collection(Constants.EVENTS_COLLECTION)
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val event = document.toObject(Event::class.java)
                    events.add(event)
                }
                adapter.notifyDataSetChanged()

                findViewById<View>(R.id.emptyView).visibility =
                    if (events.isEmpty()) View.VISIBLE else View.GONE
            }
            .addOnFailureListener { exception ->
                Toast.makeText(this, "Ошибка при получении событий.", Toast.LENGTH_SHORT).show()
            }
    }
}