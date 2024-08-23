package com.example.firebase_tut.model

data class Feedback(
    val feedbackId: String = "",
    val description: String = "",
    val uploadedBy: String = "", // User ID of the uploader
    val courseId: String = ""
)

