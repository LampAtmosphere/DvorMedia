package com.example.dvormedia

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthService {

    private val db = FirebaseFirestore.getInstance()

    fun getFirestoreInstance(): FirebaseFirestore {
        return db
    }

    fun getCurrentUserUid(): String? {
        return FirebaseAuth.getInstance().currentUser?.uid
    }

    fun registerUser(email: String, uid: String) {
        val user = hashMapOf(
            "email" to email,
            "uid" to uid,
            "role" to "user" // По умолчанию все пользователи получают роль 'user'
        )
        db.collection(Constants.USERS_COLLECTION).document(uid).set(user)
    }

    fun getUserRole(uid: String, callback: (String?) -> Unit) {
        db.collection(Constants.USERS_COLLECTION).document(uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    callback(document.getString("role"))
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { exception ->
                callback(null) // В случае ошибки, возвращаем null
            }
    }

    // Методы для входа пользователя, выхода и т.д.
}