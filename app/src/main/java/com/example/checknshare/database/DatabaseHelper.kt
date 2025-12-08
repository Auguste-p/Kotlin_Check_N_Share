package com.example.checknshare.database

import android.content.Context
import com.example.checknshare.models.Post
import com.example.checknshare.ui.notifications.Notification as UINotification

/**
 * Wrapper pour LocalDatabaseHelper (SQLite)
 * Permet d'avoir une interface cohérente pour toutes les opérations de base de données
 */
class DatabaseHelper(context: Context) {

    private val localHelper = LocalDatabaseHelper(context)

    // ========== Users ==========

    fun authenticateUser(username: String, password: String): User? {
        return localHelper.authenticateUser(username, password)
    }

    fun authenticateUserByEmail(email: String, password: String): User? {
        return localHelper.authenticateUserByEmail(email, password)
    }

    fun createUser(username: String, email: String, password: String): Boolean {
        return localHelper.createUser(username, email, password)
    }

    fun createUserReturnId(username: String, email: String, password: String): Long {
        return localHelper.createUserReturnId(username, email, password)
    }

    fun userExists(username: String): Boolean {
        return localHelper.userExists(username)
    }

    fun getAnyUserId(): Int {
        return localHelper.getAnyUserId()
    }

    // ========== Posts ==========

    fun getAllPosts(): List<Post> {
        return localHelper.getAllPosts()
    }

    fun createPost(userId: Int, imageName: String?, location: String?): Long {
        return localHelper.createPost(userId, imageName, location)
    }

    fun deletePost(postId: Int): Int {
        return localHelper.deletePost(postId)
    }

    // ========== Likes ==========

    fun getLikeCount(postId: Int): Int {
        return localHelper.getLikeCount(postId)
    }

    fun hasUserLiked(postId: Int, userId: Int): Boolean {
        return localHelper.hasUserLiked(postId, userId)
    }

    fun addLike(postId: Int, userId: Int): Long {
        return localHelper.addLike(postId, userId)
    }

    fun removeLike(postId: Int, userId: Int): Int {
        return localHelper.removeLike(postId, userId)
    }

    fun toggleLike(postId: Int, userId: Int): Boolean {
        return localHelper.toggleLike(postId, userId)
    }

    // ========== Notifications ==========

    fun getAllNotifications(recipientUserId: Int? = null): List<UINotification> {
        return localHelper.getAllNotifications(recipientUserId)
    }

    fun createNotification(
        recipientUserId: Int,
        actorName: String,
        actionText: String,
        location: String?,
        profileImageUrl: String?
    ): Long {
        return localHelper.createNotification(recipientUserId, actorName, actionText, location, profileImageUrl)
    }

    fun markNotificationAsRead(notificationId: Int): Int {
        return localHelper.markNotificationAsRead(notificationId)
    }
}
