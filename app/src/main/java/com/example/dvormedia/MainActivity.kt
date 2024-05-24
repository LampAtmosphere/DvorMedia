package com.example.dvormedia

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mxn.soul.flowingdrawer_core.ElasticDrawer
import com.mxn.soul.flowingdrawer_core.FlowingDrawer

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: FlowingDrawer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = findViewById(R.id.drawer_layout)

        val btnEvents: Button = findViewById(R.id.btn_events)
        val btnPeople: Button = findViewById(R.id.btn_people)
        val btnNotes: Button = findViewById(R.id.btn_notes)

        btnEvents.setOnClickListener {
            startActivity(Intent(this, ExampleActivity::class.java))
            drawer.closeMenu()
        }

        btnPeople.setOnClickListener {
            startActivity(Intent(this, PeopleActivity::class.java))
            drawer.closeMenu()
        }

        btnNotes.setOnClickListener {
            startActivity(Intent(this, NotesActivity::class.java))
            drawer.closeMenu()
        }
    }

    fun toggleMenu(view: View) {
        if (drawer.drawerState == ElasticDrawer.STATE_CLOSED) {
            drawer.openMenu()
        } else {
            drawer.closeMenu()
        }
    }
}