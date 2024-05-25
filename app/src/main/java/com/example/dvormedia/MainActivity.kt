package com.example.dvormedia

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.graphics.drawable.AnimatedVectorDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
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
    private lateinit var themeToggleButton: ImageButton

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
        addVersionToDrawer()

        // Setup theme toggle button
        /*val headerView = navigationView.getHeaderView(0)
        themeToggleButton = headerView.findViewById(R.id.theme_toggle_button)
        themeToggleButton.setOnClickListener {
            toggleTheme()
        }
        updateThemeIcon()*/
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
                Log.d("MainActivity", "Total people count: $totalPeopleCount")
                totalPeopleText.text = "Нас уже: $totalPeopleCount"
            } else {
                Log.e("MainActivity", "Error fetching people data", error)
            }
        }

        // Listener for today's people
        val todayDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date())
        notesListener = FirebaseFirestore.getInstance().collection("notes").whereEqualTo("date", todayDate).addSnapshotListener { documents, error ->
            if (error == null && documents != null) {
                var peopleTodayCount = 0
                for (document in documents) {
                    val people = document.get("people")
                    if (people is String) {
                        // Если данные в формате строки
                        Log.d("MainActivity", "People (String): $people")
                        peopleTodayCount += people.split(" ").filter { it.isNotEmpty() }.size
                    } else if (people is List<*>) {
                        // Если данные в формате списка
                        Log.d("MainActivity", "People (List): $people")
                        peopleTodayCount += people.sumOf { person ->
                            if (person is String) person.split(" ").filter { it.isNotEmpty() }.size else 0
                        }
                    }
                }
                Log.d("MainActivity", "Today's people count: $peopleTodayCount")
                todayPeopleText.text = "Пришло сегодня: $peopleTodayCount"
            } else {
                Log.e("MainActivity", "Error fetching today's people data", error)
            }
        }
    }

    private fun addVersionToDrawer() {
        // Получение версии приложения
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName

        // Создание TextView для версии приложения
        val versionTextView = TextView(this).apply {
            text = "Версия $versionName"
            textSize = 14f
            setPadding(16, 16, 16, 16)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.END
                setMargins(16, 16, 16, 16)
            }
        }

        // Добавление TextView в NavigationView
        navigationView.addView(versionTextView)
    }

    private fun toggleTheme() {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        val isNightMode = currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES

        // Анимация замены иконок
        val fadeOut = ObjectAnimator.ofFloat(themeToggleButton, "alpha", 1f, 0f)
        fadeOut.duration = 200

        val fadeIn = ObjectAnimator.ofFloat(themeToggleButton, "alpha", 0f, 1f)
        fadeIn.duration = 200

        fadeOut.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) {}

            override fun onAnimationEnd(animation: Animator) {
                if (isNightMode) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    animateIconChange(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_moon_to_sun) as AnimatedVectorDrawable)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    animateIconChange(ContextCompat.getDrawable(this@MainActivity, R.drawable.ic_sun_to_moon) as AnimatedVectorDrawable)
                }
                fadeIn.start()
            }

            override fun onAnimationCancel(animation: Animator) {}

            override fun onAnimationRepeat(animation: Animator) {}
        })

        AnimatorSet().apply {
            playSequentially(fadeOut, fadeIn)
            start()
        }
    }

    private fun animateIconChange(drawable: AnimatedVectorDrawable) {
        themeToggleButton.setImageDrawable(drawable)
        drawable.start()
    }

    private fun updateThemeIcon() {
        val currentNightMode = resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK
        if (currentNightMode == android.content.res.Configuration.UI_MODE_NIGHT_YES) {
            themeToggleButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_sun))
        } else {
            themeToggleButton.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_moon))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        peopleListener.remove()
        notesListener.remove()
    }
}