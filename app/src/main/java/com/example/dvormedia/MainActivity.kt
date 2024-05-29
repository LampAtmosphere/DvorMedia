package com.example.dvormedia

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GravityCompat
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
    private lateinit var totalPeopleText: TextView
    private lateinit var todayPeopleText: TextView
    private lateinit var peopleListener: ListenerRegistration
    private lateinit var notesListener: ListenerRegistration
    private lateinit var themeToggleButton: ImageButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("MainActivity", "onCreate started")
        setContentView(R.layout.activity_main)
        Log.d("MainActivity", "setContentView completed")

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        totalPeopleText = findViewById(R.id.total_people_text)
        todayPeopleText = findViewById(R.id.today_people_text)
        mainContent = findViewById(R.id.main_content)

        setupNavigationMenu()
        loadPeopleData()
        addVersionToDrawer()

        // Setup theme toggle button
        val headerView = navigationView.getHeaderView(0)
        themeToggleButton = headerView.findViewById(R.id.theme_toggle_button)
        val headerTitle: TextView = findViewById(R.id.header_title)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)

        headerTitle.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            themeToggleButton.setImageResource(R.drawable.ic_sun)
            mainContent.setBackgroundResource(R.drawable.darkbww)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeToggleButton.setImageResource(R.drawable.ic_moon)
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)
        }

        themeToggleButton.setOnClickListener {
            animateButtonClick(it)
            toggleTheme()
        }
    }

    private fun setupNavigationMenu() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            FirebaseFirestore.getInstance().collection("users").document(userId).get().addOnSuccessListener { document ->
                if (document.exists()) {
                    val role = document.getString("role")
                    if (role == "admin") {
                        val newEventMenuItem = navigationView.menu.findItem(R.id.nav_new_event_activity)
                        newEventMenuItem.isVisible = true

                        // Установка шрифта и стиля текста
                        val typeface = ResourcesCompat.getFont(this, R.font.handyman)
                        if (typeface != null) {
                            val spannableString = SpannableString(newEventMenuItem.title)
                            spannableString.setSpan(CustomTypefaceSpan("", typeface, Typeface.BOLD), 0, spannableString.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                            newEventMenuItem.title = spannableString
                        }
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

    private fun animateButtonClick(view: View) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f)
        scaleDownX.duration = 200
        scaleDownY.duration = 200

        val scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f)
        val scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f)
        scaleUpX.duration = 200
        scaleUpY.duration = 200

        val scaleDown = AnimatorSet()
        scaleDown.play(scaleDownX).with(scaleDownY)

        val scaleUp = AnimatorSet()
        scaleUp.play(scaleUpX).with(scaleUpY)

        val scaleDownUp = AnimatorSet()
        scaleDownUp.play(scaleDown).before(scaleUp)
        scaleDownUp.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                view.isClickable = true
            }

            override fun onAnimationStart(animation: Animator) {
                super.onAnimationStart(animation)
                view.isClickable = false
            }
        })
        scaleDownUp.start()
    }

    private fun toggleTheme() {
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeToggleButton.setImageResource(R.drawable.ic_moon)
            mainContent.setBackgroundResource(R.drawable.photo_2024_05_24_22_41_27)

            val header = findViewById<RelativeLayout>(R.id.header_layout)
            header.background = ColorDrawable(resources.getColor(R.color.header_background_dark))
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            themeToggleButton.setImageResource(R.drawable.ic_sun)
            mainContent.setBackgroundResource(R.drawable.darkbww)
        }

        // Save theme preference
        with(sharedPreferences.edit()) {
            putBoolean("isNightMode", !isNightMode)
            apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        peopleListener.remove()
        notesListener.remove()
    }
}