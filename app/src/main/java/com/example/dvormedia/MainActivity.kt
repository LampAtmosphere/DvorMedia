package com.example.dvormedia

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar
    private lateinit var drawerToggle: ActionBarDrawerToggle
    private lateinit var totalPeopleText: TextView
    private lateinit var todayPeopleText: TextView
    private lateinit var peopleListener: ListenerRegistration
    private lateinit var notesListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        totalPeopleText = findViewById(R.id.total_people_text)
        todayPeopleText = findViewById(R.id.today_people_text)

        drawerToggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()

        setupNavigationMenu()
        loadPeopleData()
    }

    private fun setupNavigationMenu() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        navigationView.menu.findItem(R.id.nav_new_event_activity).isVisible = true
                    }
                }
            }.addOnFailureListener {
                // Обработка ошибки
            }
        }

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_example_activity -> {
                    startActivity(Intent(this, ExampleActivity::class.java))
                }
                R.id.nav_new_event_activity -> {
                    startActivity(Intent(this, NewEventActivity::class.java))
                }
                R.id.nav_people_activity -> {
                    startActivity(Intent(this, PeopleActivity::class.java))
                }
                R.id.nav_note_activity -> {
                    startActivity(Intent(this, NotesActivity::class.java))
                }
            }
            drawerLayout.closeDrawers()
            true
        }
    }

    private fun loadPeopleData() {
        // Listener for total people
        peopleListener = FirebaseFirestore.getInstance().collection("people").addSnapshotListener { documents, error ->
            if (error == null && documents != null) {
                val totalPeopleCount = documents.size()
                totalPeopleText.text = "Total People: $totalPeopleCount"
            }
        }

        // Listener for today's people
        val todayDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        notesListener = FirebaseFirestore.getInstance().collection("notes").whereEqualTo("date", todayDate).addSnapshotListener { documents, error ->
            if (error == null && documents != null) {
                var peopleTodayCount = 0
                for (document in documents) {
                    val peopleList = document.get("people") as? List<*>
                    peopleTodayCount += peopleList?.size ?: 0
                }
                todayPeopleText.text = "People Today: $peopleTodayCount"
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        peopleListener.remove()
        notesListener.remove()
    }
}