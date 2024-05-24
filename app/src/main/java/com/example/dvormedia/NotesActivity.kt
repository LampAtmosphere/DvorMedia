package com.example.dvormedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotesAdapter
    private val notes = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = NotesAdapter(notes)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@NotesActivity)
            adapter = this@NotesActivity.adapter
        }

        // Добавьте заметку с сегодняшней датой
        val currentDate = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date())
        notes.add("Заметка от $currentDate")
        adapter.notifyDataSetChanged()
    }
}