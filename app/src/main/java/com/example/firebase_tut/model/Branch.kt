package com.example.firebase_tut.model

data class Branch(
    val branchId: String = "",
    val collegeId: String = "",
    val branchName: String = "",
    val courses: List<String> = listOf() // List of course IDs
)