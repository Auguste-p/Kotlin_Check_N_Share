package com.example.checknshare.database

import android.content.Context
import android.util.Log
import com.example.checknshare.models.Post
import com.example.checknshare.ui.notifications.Notification as UINotification
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Helper pour gérer la base de données Supabase
 * Remplace LocalDatabaseHelper pour utiliser Supabase au lieu de SQLite
 */
class SupabaseDatabaseHelper(private val context: Context) {

    companion object {
        private const val TAG = "SupabaseDatabaseHelper"
    }

    // Client Supabase
    private val supabase = createSupabaseClient(
        supabaseUrl = DatabaseConfig.SUPABASE_URL,
        supabaseKey = DatabaseConfig.SUPABASE_API_KEY
    ) {
        install(Postgrest)
    }

    // Models pour Supabase (doivent correspondre aux tables)
    @Serializable
    data class UserRow(
        val id: Int? = null,
        val username: String,
        val email: String,
        val password: String,
        @SerialName("created_at") val createdAt: String? = null
    )

    @Serializable
    data class PostRow(
        val id: Int? = null,
        @SerialName("userId") val userId: Int,
        @SerialName("imageName") val imageName: String?,
        val location: String?,
        @SerialName("location_address") val locationAddress: String?,
        @SerialName("created_at") val createdAt: String? = null
    )

    @Serializable
    data class LikeRow(
        val id: Int? = null,
        @SerialName("post_id") val postId: Int,
        @SerialName("user_id") val userId: Int
    )

    @Serializable
    data class NotificationRow(
        val id: Int? = null,
        @SerialName("recipient_user_id") val recipientUserId: Int,
        @SerialName("actor_name") val actorName: String,
        @SerialName("action_text") val actionText: String,
        val location: String?,
        @SerialName("time_ago") val timeAgo: String,
        @SerialName("profile_image") val profileImage: String?,
        @SerialName("is_read") val isRead: Int = 0
    )

    @Serializable
    data class PostWithUser(
        val id: Int,
        @SerialName("userId") val userId: Int,
        val username: String,
        @SerialName("imageName") val imageName: String?,
        val location: String?,
        @SerialName("location_address") val locationAddress: String?,
        @SerialName("created_at") val createdAt: String?
    )

    // Hash password
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // ========== Users ==========

    suspend fun authenticateUser(email: String, password: String): User? = withContext(Dispatchers.IO) {
        try {
            val hashedPassword = hashPassword(password)
            val result = supabase.from("users")
                .select {
                    filter {
                        eq("email", email)
                        eq("password", hashedPassword)
                    }
                }
                .decodeSingle<UserRow>()

            User(
                id = result.id ?: -1,
                username = result.username,
                email = result.email
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error authenticating user", e)
            null
        }
    }

    suspend fun createUser(username: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val hashedPassword = hashPassword(password)
            supabase.from("users").insert(
                UserRow(
                    username = username,
                    email = email,
                    password = hashedPassword
                )
            )
            Log.d(TAG, "User created: $username")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            false
        }
    }

    suspend fun createUserReturnId(username: String, email: String, password: String): Long = withContext(Dispatchers.IO) {
        try {
            val hashedPassword = hashPassword(password)
            val result = supabase.from("users").insert(
                UserRow(
                    username = username,
                    email = email,
                    password = hashedPassword
                )
            ) {
                select()
            }.decodeSingle<UserRow>()

            result.id?.toLong() ?: -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating user", e)
            -1L
        }
    }

    suspend fun userExists(username: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("users")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("username", username)
                    }
                }
                .decodeList<UserRow>()

            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user exists", e)
            false
        }
    }

    suspend fun getAnyUserId(): Int = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("users")
                .select(columns = Columns.list("id")) {
                    limit(1)
                }
                .decodeSingle<UserRow>()

            result.id ?: -1
        } catch (e: Exception) {
            Log.e(TAG, "Error getting any user id", e)
            -1
        }
    }

    // ========== Posts ==========

    suspend fun getAllPosts(): List<Post> = withContext(Dispatchers.IO) {
        try {
            // Requête avec jointure sur users
            val result = supabase.from("posts")
                .select(columns = Columns.raw("""
                    id,
                    userId,
                    users!inner(username),
                    imageName,
                    location,
                    location_address,
                    created_at
                """.trimIndent())) {
                    order("created_at", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }
                .decodeList<Map<String, Any?>>()

            result.map { row ->
                val users = row["users"] as? Map<*, *>
                Post(
                    id = (row["id"] as? Number)?.toInt() ?: 0,
                    userId = (row["userId"] as? Number)?.toInt() ?: 0,
                    username = (users?.get("username") as? String) ?: "",
                    imageName = row["imageName"] as? String,
                    location = row["location"] as? String,
                    locationAddress = row["location_address"] as? String,
                    createdAt = row["created_at"] as? String
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all posts", e)
            emptyList()
        }
    }

    suspend fun createPost(userId: Int, imageName: String?, location: String?): Long = withContext(Dispatchers.IO) {
        try {
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")
            val currentDate = LocalDateTime.now().format(formatter)

            val result = supabase.from("posts").insert(
                PostRow(
                    userId = userId,
                    imageName = imageName,
                    location = location,
                    locationAddress = location,
                    createdAt = currentDate
                )
            ) {
                select()
            }.decodeSingle<PostRow>()

            result.id?.toLong() ?: -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating post", e)
            -1L
        }
    }

    suspend fun deletePost(postId: Int): Int = withContext(Dispatchers.IO) {
        try {
            // Supprimer d'abord les likes associés
            supabase.from("likes").delete {
                filter {
                    eq("post_id", postId)
                }
            }

            // Puis supprimer le post
            supabase.from("posts").delete {
                filter {
                    eq("id", postId)
                }
            }
            1 // Success
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting post", e)
            0
        }
    }

    // ========== Likes ==========

    suspend fun getLikeCount(postId: Int): Int = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("likes")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("post_id", postId)
                    }
                }
                .decodeList<LikeRow>()

            result.size
        } catch (e: Exception) {
            Log.e(TAG, "Error getting like count", e)
            0
        }
    }

    suspend fun hasUserLiked(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("likes")
                .select(columns = Columns.list("id")) {
                    filter {
                        eq("post_id", postId)
                        eq("user_id", userId)
                    }
                }
                .decodeList<LikeRow>()

            result.isNotEmpty()
        } catch (e: Exception) {
            Log.e(TAG, "Error checking if user liked", e)
            false
        }
    }

    suspend fun addLike(postId: Int, userId: Int): Long = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("likes").insert(
                LikeRow(
                    postId = postId,
                    userId = userId
                )
            ) {
                select()
            }.decodeSingle<LikeRow>()

            result.id?.toLong() ?: -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error adding like", e)
            -1L
        }
    }

    suspend fun removeLike(postId: Int, userId: Int): Int = withContext(Dispatchers.IO) {
        try {
            supabase.from("likes").delete {
                filter {
                    eq("post_id", postId)
                    eq("user_id", userId)
                }
            }
            1 // Success
        } catch (e: Exception) {
            Log.e(TAG, "Error removing like", e)
            0
        }
    }

    suspend fun toggleLike(postId: Int, userId: Int): Boolean = withContext(Dispatchers.IO) {
        if (hasUserLiked(postId, userId)) {
            removeLike(postId, userId)
            false
        } else {
            addLike(postId, userId)
            true
        }
    }

    // ========== Notifications ==========

    suspend fun getAllNotifications(recipientUserId: Int? = null): List<UINotification> = withContext(Dispatchers.IO) {
        try {
            val query = supabase.from("notifications")
                .select {
                    if (recipientUserId != null) {
                        filter {
                            eq("recipient_user_id", recipientUserId)
                        }
                    }
                    order("id", order = io.github.jan.supabase.postgrest.query.Order.DESCENDING)
                }

            val result = query.decodeList<NotificationRow>()

            result.map { row ->
                UINotification(
                    id = row.id ?: 0,
                    userName = row.actorName,
                    action = row.actionText,
                    location = row.location ?: "",
                    timeAgo = row.timeAgo,
                    profileImageUrl = row.profileImage
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting notifications", e)
            emptyList()
        }
    }

    suspend fun createNotification(
        recipientUserId: Int,
        actorName: String,
        actionText: String,
        location: String?,
        profileImageUrl: String?
    ): Long = withContext(Dispatchers.IO) {
        try {
            val result = supabase.from("notifications").insert(
                NotificationRow(
                    recipientUserId = recipientUserId,
                    actorName = actorName,
                    actionText = actionText,
                    location = location,
                    timeAgo = "à l'instant",
                    profileImage = profileImageUrl,
                    isRead = 0
                )
            ) {
                select()
            }.decodeSingle<NotificationRow>()

            result.id?.toLong() ?: -1L
        } catch (e: Exception) {
            Log.e(TAG, "Error creating notification", e)
            -1L
        }
    }

    suspend fun markNotificationAsRead(notificationId: Int): Int = withContext(Dispatchers.IO) {
        try {
            supabase.from("notifications").update(
                mapOf("is_read" to 1)
            ) {
                filter {
                    eq("id", notificationId)
                }
            }
            1 // Success
        } catch (e: Exception) {
            Log.e(TAG, "Error marking notification as read", e)
            0
        }
    }
}

