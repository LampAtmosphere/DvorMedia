package com.example.dvormedia

data class Person(
    val name: String = "",
    var id: String = "" // Добавьте это поле для хранения идентификатора документа Firestore
)