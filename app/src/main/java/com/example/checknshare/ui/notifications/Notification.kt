package com.example.checknshare.ui.notifications

data class Notification(
    val id: Int,
    val userName: String,
    val action: String,
    val location: String,
    val timeAgo: String,
    val profileImageUrl: String? = null
)

