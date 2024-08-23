package com.example.firebase_tut.model

data class Course(
    val courseId: String = "",
    val courseName: String = "",
    val branchId: String = "",
    val resources: List<String> = listOf(), // List of resource IDs
    val feedback: List<String> = listOf() // List of feedback IDs
)

