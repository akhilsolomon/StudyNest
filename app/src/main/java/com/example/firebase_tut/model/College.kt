package com.example.firebase_tut.model

data class College(
    val collegeId: String = "",
    val collegeName: String = "",
    val adminId: String = "",
    val branches: List<String> = listOf() // List of branch IDs
)