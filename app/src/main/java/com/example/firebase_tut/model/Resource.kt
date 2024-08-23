package com.example.firebase_tut.model

data class Resource(
    val resourceId: String = "",
    val resourceName: String = "",
    var courseId: String = "",
    val resourceUrl: String = "",
    val approved: Boolean = false,
    val description: String = "",
    val uploadedBy: String = "" // User ID of the uploader
)

