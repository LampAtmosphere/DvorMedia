package com.example.dvormedia

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class PeopleActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PeopleAdapter
    private val people = mutableListOf<String>() // Список людей

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_people)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = PeopleAdapter(people)
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PeopleActivity)
            adapter = this@PeopleActivity.adapter
        }

        // Добавьте людей в список
        people.addAll(listOf("Иван Иванов", "Петр Петров", "Сидор Сидоров"))
        adapter.notifyDataSetChanged()
    }
}