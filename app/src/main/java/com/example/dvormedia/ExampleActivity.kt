package com.example.dvormedia

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore

class ExampleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: EventsAdapter
    private val events = mutableListOf<Event>()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example)

        recyclerView = findViewById(R.id.recyclerView)

        adapter = EventsAdapter(events)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ExampleActivity)
            adapter = this@ExampleActivity.adapter
            addItemDecoration(DividerItemDecoration(this@ExampleActivity, DividerItemDecoration.VERTICAL))
        }

        fetchEvents()
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












