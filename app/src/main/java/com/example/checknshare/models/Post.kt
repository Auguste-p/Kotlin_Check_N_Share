package com.example.checknshare.models

data class Post(
    val id: Int,
    val userId: Int,
    val username: String,
    val imageName: String?, // nom du drawable (ex: "photo_placeholder_1")
    val location: String?,
    val locationAddress: String?,
    val createdAt: String? // format SQLite: "YYYY-MM-DD HH:MM:SS"
)
