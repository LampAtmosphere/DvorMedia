package com.example.dvormedia

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
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
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var recyclerView: RecyclerView
    private lateinit var totalPeopleText: TextView
    private lateinit var todayPeopleText: TextView
    private lateinit var peopleListener: ListenerRegistration
    private lateinit var notesListener: ListenerRegistration
    private lateinit var themeToggleButton: ImageButton
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var mainContent: FrameLayout
    private lateinit var photosAdapter: PhotosAdapter
    private lateinit var photosList: MutableList<Photo>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("com.example.dvormedia.MainActivity", "onCreate started")
        setContentView(R.layout.activity_main)
        Log.d("com.example.dvormedia.MainActivity", "setContentView completed")

        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)
        recyclerView = findViewById(R.id.recycler_view)
        /*totalPeopleText = findViewById(R.id.total_people_text)
        todayPeopleText = findViewById(R.id.today_people_text)*/
        mainContent = findViewById(R.id.main_content)

        // Настройка RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        photosList = mutableListOf()
        photosAdapter = PhotosAdapter(this, photosList) // Передача контекста и списка фотографий
        recyclerView.adapter = photosAdapter

        loadPhotosFromFirebase()

        // Настройка кнопки для перехода по ссылке
        val buttonLinkVk: ImageButton = findViewById(R.id.buttonLinkVk)
        buttonLinkVk.setOnClickListener {
            openWebPage("https://vk.com/dvorpnz")
        }
        val buttonLinkTg: ImageButton = findViewById(R.id.buttonLinkTg)
        buttonLinkTg.setOnClickListener {
            openWebPage("https://t.me/+cY7UCk-KxBI5OTgy")
        }

        // Настройка кнопки для открытия DrawerLayout
        val buttonOpenDrawer: ImageButton = findViewById(R.id.buttonOpenDrawer)
        buttonOpenDrawer.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        setupNavigationMenu()
        /*loadPeopleData()*/
        addVersionToDrawer()

        // Setup theme toggle button
        val headerView = navigationView.getHeaderView(0)
        themeToggleButton = headerView.findViewById(R.id.theme_toggle_button)
        sharedPreferences = getSharedPreferences("theme_prefs", MODE_PRIVATE)

        // Set initial theme state
        val isNightMode = sharedPreferences.getBoolean("isNightMode", false)
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            themeToggleButton.setImageResource(R.drawable.ic_sun)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            themeToggleButton.setImageResource(R.drawable.ic_moon)
        }

        themeToggleButton.setOnClickListener {
            toggleTheme()
        }
    }

    private fun loadPhotosFromFirebase() {
        val storageReference = FirebaseStorage.getInstance().reference.child("events_media/photos/")
        storageReference.listAll().addOnSuccessListener { listResult ->
            val allItems = listResult.items
            if (allItems.size > 7) {
                allItems.shuffle()
            }
            val randomItems = allItems.take(7)

            for (item in randomItems) {
                item.downloadUrl.addOnSuccessListener { uri ->
                    photosList.add(Photo(uri.toString(), ""))
                    photosAdapter.notifyDataSetChanged()
                }.addOnFailureListener { exception ->
                    Log.e("com.example.dvormedia.MainActivity", "Error getting download URL", exception)
                }
            }
        }.addOnFailureListener { exception ->
            Log.e("com.example.dvormedia.MainActivity", "Error getting items from Firebase Storage", exception)
        }
    }

    private fun setupNavigationMenu() {
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

        // Fetch user role from Firestore and update the menu
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val db = FirebaseFirestore.getInstance()
            val docRef = db.collection("users").document(it.uid)
            docRef.get().addOnSuccessListener { document ->
                if (document != null && document.exists()) {
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
        // Создание TextView для "LampAtmosphere" и "shast_0n"
        val additionalTextView = TextView(this).apply {
            text = "LampAtmosphere\nshast_0n"
            textSize = 12f
            setTextColor(Color.parseColor("#77818181")) // Полупрозрачный белый цвет
            setPadding(16, 16, 16, 16)
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.BOTTOM or Gravity.START
                setMargins(16, 16, 16, 16)
            }
        }

        // Добавление TextView в NavigationView
        navigationView.addView(versionTextView)
        navigationView.addView(additionalTextView)
    }

    private fun animateButtonClick(view: View) {
        val scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 0.7f)
        val scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 0.7f)
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

    private fun openWebPage(url: String) {
        val webpage: Uri = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Log.e("com.example.dvormedia.MainActivity", "Невозможно открыть ссылку: $url")
        }
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
        /*peopleListener.remove()
        notesListener.remove()*/
    }
}