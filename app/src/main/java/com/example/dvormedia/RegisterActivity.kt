package com.example.dvormedia

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.dvormedia.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val db = FirebaseFirestore.getInstance() // Инициализация Firestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            createNewUser(binding.editTextNewEmail.text.toString(), binding.editTextNewPassword.text.toString())
            animateButtonClick(it)
        }
    }

    private fun createNewUser(email: String, password: String) {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = task.result?.user // получаем информацию о пользователе
                    user?.let {
                        registerUser(email, it.uid) // Регистрация пользователя в Firestore
                    }
                    Toast.makeText(this, "Registration successful.", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Если регистрация не удалась, выведите сообщение для пользователя
                    Toast.makeText(baseContext, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun registerUser(email: String, uid: String) {
        val user = hashMapOf(
            "email" to email,
            "uid" to uid,
            "role" to "user" // По умолчанию все пользователи получают роль 'user'
        )
        db.collection("users").document(uid).set(user) // добавление пользователя в Firestore
            .addOnSuccessListener {
                Toast.makeText(this, "User added to Firestore successfully", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.d("err", "Error adding user to Firestore: ${e.message}")
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