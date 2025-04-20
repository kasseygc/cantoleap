package com.example.project.data.models

data class Note(
    var id: String? = null,
    val userId: String = "",
    val userEmail: String = "",
    val note: String = "",
    val upvotes: Int = 0,
    val downvotes: Int = 0,
    val timestamp: Long = 0
)