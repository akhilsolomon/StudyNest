package com.example.firebase_tut.model

data class User(
    val userId: String = "",
    val userName: String = "",
    val password: String = "",
    val email: String = "",
    val role: String = "", // "admin" or "user"
    val approved: Boolean = false,
    val collegeId: String = ""
)

