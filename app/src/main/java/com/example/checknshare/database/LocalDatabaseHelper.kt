package com.example.checknshare.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.example.checknshare.models.Post
import com.example.checknshare.ui.notifications.Notification as UINotification
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val TAG = "LocalDatabaseHelper"
        private const val DATABASE_NAME = "checknshare.db"
        // Version bumpée pour forcer onUpgrade et recréation de la DB avec les bons hashs
        private const val DATABASE_VERSION = 3

        // Users
        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_CREATED_AT = "created_at"

        // Posts
        private const val TABLE_POSTS = "posts"
        private const val COLUMN_POST_ID = "id"
        private const val COLUMN_POST_USER_ID = "userId"
        private const val COLUMN_POST_IMAGE = "imageName"
        private const val COLUMN_POST_LOCATION = "location"
        private const val COLUMN_POST_LOCATION_ADDRESS = "location_address"
        private const val COLUMN_POST_CREATED_AT = "created_at"

        // Likes
        private const val TABLE_LIKES = "likes"
        private const val COLUMN_LIKE_ID = "id"
        private const val COLUMN_LIKE_POST_ID = "post_id"
        private const val COLUMN_LIKE_USER_ID = "user_id"

        // Notifications
        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val COLUMN_NOTIFICATION_ID = "id"
        private const val COLUMN_NOTIFICATION_RECIPIENT = "recipient_user_id"
        private const val COLUMN_NOTIFICATION_ACTOR = "actor_name"
        private const val COLUMN_NOTIFICATION_ACTION = "action_text"
        private const val COLUMN_NOTIFICATION_LOCATION = "location"
        private const val COLUMN_NOTIFICATION_TIMEAGO = "time_ago"
        private const val COLUMN_NOTIFICATION_PROFILE = "profile_image"
        private const val COLUMN_NOTIFICATION_IS_READ = "is_read"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        Log.d(TAG, "onCreate: creating tables if not exists")
        // Users table
        val createUsers = """
            CREATE TABLE IF NOT EXISTS $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        // Posts table
        val createPosts = """
            CREATE TABLE IF NOT EXISTS $TABLE_POSTS (
                $COLUMN_POST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_POST_USER_ID INTEGER NOT NULL,
                $COLUMN_POST_IMAGE TEXT,
                $COLUMN_POST_LOCATION TEXT,
                $COLUMN_POST_LOCATION_ADDRESS TEXT,
                $COLUMN_POST_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_POST_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        // Likes table
        val createLikes = """
            CREATE TABLE IF NOT EXISTS $TABLE_LIKES (
                $COLUMN_LIKE_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_LIKE_POST_ID INTEGER NOT NULL,
                $COLUMN_LIKE_USER_ID INTEGER NOT NULL,
                FOREIGN KEY($COLUMN_LIKE_POST_ID) REFERENCES $TABLE_POSTS($COLUMN_POST_ID),
                FOREIGN KEY($COLUMN_LIKE_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        db?.execSQL(createUsers)
        db?.execSQL(createPosts)
        db?.execSQL(createLikes)

        // Seed: créer un utilisateur de test
        val hashedPassword = hashPassword("test123")
        val userValues = ContentValues().apply {
            put(COLUMN_USERNAME, "test")
            put(COLUMN_EMAIL, "test@example.com")
            put(COLUMN_PASSWORD, hashedPassword)
        }
        val userId = db?.insert(TABLE_USERS, null, userValues) ?: -1L

        // Seed: quelques posts exemples (utiliser des drawables existants)
        val sampleImages = listOf("photo_placeholder_1", "photo_placeholder_2", "photo_placeholder_3")
        for (i in 0 until 3) {
            val postValues = ContentValues().apply {
                put(COLUMN_POST_USER_ID, userId.toInt())
                put(COLUMN_POST_IMAGE, sampleImages[i % sampleImages.size])
                put(COLUMN_POST_LOCATION, "Paris, France")
                put(COLUMN_POST_LOCATION_ADDRESS, "Adresse Exemple $i")
            }
            val postId = db?.insert(TABLE_POSTS, null, postValues) ?: -1L

            // Seed: ajouter quelques likes sur les posts
            val likeCount = (i + 1) * 5
            for (j in 0 until likeCount) {
                val likeValues = ContentValues().apply {
                    put(COLUMN_LIKE_POST_ID, postId.toInt())
                    put(COLUMN_LIKE_USER_ID, userId.toInt())
                }
                db?.insert(TABLE_LIKES, null, likeValues)
            }
        }

        // Notifications table (create and seed)
        val createNotifications = """
            CREATE TABLE IF NOT EXISTS $TABLE_NOTIFICATIONS (
                $COLUMN_NOTIFICATION_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NOTIFICATION_RECIPIENT INTEGER NOT NULL,
                $COLUMN_NOTIFICATION_ACTOR TEXT,
                $COLUMN_NOTIFICATION_ACTION TEXT,
                $COLUMN_NOTIFICATION_LOCATION TEXT,
                $COLUMN_NOTIFICATION_TIMEAGO TEXT,
                $COLUMN_NOTIFICATION_PROFILE TEXT,
                $COLUMN_NOTIFICATION_IS_READ INTEGER DEFAULT 0,
                FOREIGN KEY($COLUMN_NOTIFICATION_RECIPIENT) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()

        db?.execSQL(createNotifications)

        // Seed: créer quelques notifications d'exemple pour l'utilisateur test
        val sampleNotifications = listOf(
            UINotification(1, "Marie Dubois", "a publié une photo", "Tour Eiffel, Paris", "5 min"),
            UINotification(2, "Alex Martin", "a publié une photo", "Le Comptoir du Relais, Paris", "1h"),
            UINotification(3, "Sophie Bernard", "a commenté votre photo", "Musée du Louvre, Paris", "2h"),
            UINotification(4, "Thomas Laurent", "a aimé votre photo", "Arc de Triomphe, Paris", "3h")
        )

        for (n in sampleNotifications) {
            val notifValues = ContentValues().apply {
                put(COLUMN_NOTIFICATION_RECIPIENT, userId.toInt())
                put(COLUMN_NOTIFICATION_ACTOR, n.userName)
                put(COLUMN_NOTIFICATION_ACTION, n.action)
                put(COLUMN_NOTIFICATION_LOCATION, n.location)
                put(COLUMN_NOTIFICATION_TIMEAGO, n.timeAgo)
                put(COLUMN_NOTIFICATION_PROFILE, n.profileImageUrl)
            }
            db?.insert(TABLE_NOTIFICATIONS, null, notifValues)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        Log.d(TAG, "onUpgrade: upgrading DB from $oldVersion to $newVersion - dropping and recreating tables (dev)")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LIKES")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_POSTS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        onCreate(db)
    }

    override fun onOpen(db: SQLiteDatabase?) {
        super.onOpen(db)
        Log.d(TAG, "onOpen: ensuring posts table exists")
        // Create posts table if it's missing (safe no-op if already present)
        val ensurePosts = """
            CREATE TABLE IF NOT EXISTS $TABLE_POSTS (
                $COLUMN_POST_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_POST_USER_ID INTEGER NOT NULL,
                $COLUMN_POST_IMAGE TEXT,
                $COLUMN_POST_LOCATION TEXT,
                $COLUMN_POST_LOCATION_ADDRESS TEXT,
                $COLUMN_POST_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY($COLUMN_POST_USER_ID) REFERENCES $TABLE_USERS($COLUMN_ID)
            )
        """.trimIndent()
        db?.execSQL(ensurePosts)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Authentifier un utilisateur par username
    fun authenticateUser(username: String, password: String): User? {
        val db = readableDatabase
        val hashedPassword = hashPassword(password)

        Log.d(TAG, "authenticateUser: Attempting to authenticate user: $username")
        Log.d(TAG, "authenticateUser: Hashed password: $hashedPassword")

        // D'abord vérifier si l'utilisateur existe
        val userCheckCursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )

        if (userCheckCursor.moveToFirst()) {
            val storedPassword = userCheckCursor.getString(userCheckCursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            Log.d(TAG, "authenticateUser: User found. Stored password hash: $storedPassword")
            Log.d(TAG, "authenticateUser: Password match: ${storedPassword == hashedPassword}")
            userCheckCursor.close()
        } else {
            Log.d(TAG, "authenticateUser: User not found in database")
            userCheckCursor.close()
            return null
        }

        // Authentification avec mot de passe
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL),
            "$COLUMN_USERNAME = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(username, hashedPassword),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            )
            cursor.close()
            Log.d(TAG, "authenticateUser: Authentication successful for user: $username")
            user
        } else {
            cursor.close()
            Log.d(TAG, "authenticateUser: Authentication failed - password mismatch")
            null
        }
    }

    // Authentifier un utilisateur par email
    fun authenticateUserByEmail(email: String, password: String): User? {
        val db = readableDatabase
        val hashedPassword = hashPassword(password)

        Log.d(TAG, "authenticateUserByEmail: Attempting to authenticate user with email: $email")
        Log.d(TAG, "authenticateUserByEmail: Hashed password: $hashedPassword")

        // D'abord vérifier si l'utilisateur existe
        val userCheckCursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL, COLUMN_PASSWORD),
            "$COLUMN_EMAIL = ?",
            arrayOf(email),
            null, null, null
        )

        if (userCheckCursor.moveToFirst()) {
            val storedPassword = userCheckCursor.getString(userCheckCursor.getColumnIndexOrThrow(COLUMN_PASSWORD))
            Log.d(TAG, "authenticateUserByEmail: User found. Stored password hash: $storedPassword")
            Log.d(TAG, "authenticateUserByEmail: Password match: ${storedPassword == hashedPassword}")
            userCheckCursor.close()
        } else {
            Log.d(TAG, "authenticateUserByEmail: User not found in database")
            userCheckCursor.close()
            return null
        }

        // Authentification avec mot de passe
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID, COLUMN_USERNAME, COLUMN_EMAIL),
            "$COLUMN_EMAIL = ? AND $COLUMN_PASSWORD = ?",
            arrayOf(email, hashedPassword),
            null, null, null
        )

        return if (cursor.moveToFirst()) {
            val user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)),
                username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)),
                email = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_EMAIL))
            )
            cursor.close()
            Log.d(TAG, "authenticateUserByEmail: Authentication successful for email: $email")
            user
        } else {
            cursor.close()
            Log.d(TAG, "authenticateUserByEmail: Authentication failed - password mismatch")
            null
        }
    }

    // Créer un nouvel utilisateur (retourne true/false)
    fun createUser(username: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val hashedPassword = hashPassword(password)

        Log.d(TAG, "createUser: Creating user: $username with email: $email")
        Log.d(TAG, "createUser: Hashed password: $hashedPassword")

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, hashedPassword)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)
            if (result != -1L) {
                Log.d(TAG, "createUser: User created successfully with ID: $result")
                true
            } else {
                Log.e(TAG, "createUser: Failed to insert user")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "createUser: Exception occurred: ${e.message}", e)
            false
        }
    }

    // Créer un nouvel utilisateur et retourner l'id inséré (ou -1 si erreur)
    fun createUserReturnId(username: String, email: String, password: String): Long {
        val db = writableDatabase
        val hashedPassword = hashPassword(password)

        Log.d(TAG, "createUserReturnId: Creating user: $username with email: $email")
        Log.d(TAG, "createUserReturnId: Hashed password: $hashedPassword")

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, hashedPassword)
        }
        return try {
            val result = db.insert(TABLE_USERS, null, values)
            Log.d(TAG, "createUserReturnId: User created with ID: $result")
            result
        } catch (e: Exception) {
            Log.e(TAG, "createUserReturnId: Exception occurred: ${e.message}", e)
            -1L
        }
    }

    // Vérifier si un utilisateur existe
    fun userExists(username: String): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            "$COLUMN_USERNAME = ?",
            arrayOf(username),
            null, null, null
        )

        val exists = cursor.count > 0
        cursor.close()
        return exists
    }

    // Récupérer tous les posts (avec username)
    fun getAllPosts(): List<Post> {
        val posts = mutableListOf<Post>()
        val db = readableDatabase
        val query = "SELECT p.$COLUMN_POST_ID as id, p.$COLUMN_POST_USER_ID as user_id, u.$COLUMN_USERNAME as username, p.$COLUMN_POST_IMAGE as image_name, p.$COLUMN_POST_LOCATION as location, p.$COLUMN_POST_LOCATION as location_address, p.$COLUMN_POST_CREATED_AT as created_at FROM $TABLE_POSTS p JOIN $TABLE_USERS u ON p.$COLUMN_POST_USER_ID = u.$COLUMN_ID ORDER BY p.$COLUMN_POST_CREATED_AT DESC"
        val cursor = db.rawQuery(query, null)
        if (cursor.moveToFirst()) {
            do {
                val post = Post(
                    id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                    userId = cursor.getInt(cursor.getColumnIndexOrThrow("user_id")),
                    username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                    imageName = cursor.getString(cursor.getColumnIndexOrThrow("image_name")),
                    location = cursor.getString(cursor.getColumnIndexOrThrow("location")),
                    locationAddress = cursor.getString(cursor.getColumnIndexOrThrow("location_address")),
                    createdAt = cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                )
                posts.add(post)
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d(TAG, "getAllPosts: retrieved ${posts.size} posts");
        return posts
    }

    // Obtenir le nombre de likes pour un post
    fun getLikeCount(postId: Int): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LIKES,
            arrayOf("COUNT(*) as cnt"),
            "$COLUMN_LIKE_POST_ID = ?",
            arrayOf(postId.toString()),
            null, null, null
        )
        var count = 0
        if (cursor.moveToFirst()) {
            count = cursor.getInt(cursor.getColumnIndexOrThrow("cnt"))
        }
        cursor.close()
        return count
    }

    // Créer un post
    fun createPost(userId: Int, imageName: String?, location: String?): Long {
        val db = writableDatabase
        val formatter = DateTimeFormatter.ofPattern("dd-MM-yyy HH:mm")
        val currentDate = LocalDateTime.now().format(formatter)

        val values = ContentValues().apply {
            put(COLUMN_POST_USER_ID, userId)
            put(COLUMN_POST_IMAGE, imageName)
            put(COLUMN_POST_LOCATION, location)
            put(COLUMN_POST_LOCATION_ADDRESS, location)
            put(COLUMN_POST_CREATED_AT, currentDate)
        }
        return db.insert(TABLE_POSTS, null, values)
    }

    // Supprimer un post (et ses likes associés en cascade grâce aux FK)
    fun deletePost(postId: Int): Int {
        val db = writableDatabase
        // Supprimer d'abord les likes associés
        db.delete(TABLE_LIKES, "$COLUMN_LIKE_POST_ID = ?", arrayOf(postId.toString()))
        // Puis supprimer le post
        return db.delete(TABLE_POSTS, "$COLUMN_POST_ID = ?", arrayOf(postId.toString()))
    }

    // Vérifier si un utilisateur a liké un post
    fun hasUserLiked(postId: Int, userId: Int): Boolean {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_LIKES,
            arrayOf(COLUMN_LIKE_ID),
            "$COLUMN_LIKE_POST_ID = ? AND $COLUMN_LIKE_USER_ID = ?",
            arrayOf(postId.toString(), userId.toString()),
            null, null, null
        )
        val liked = cursor.count > 0
        cursor.close()
        return liked
    }

    // Ajouter un like
    fun addLike(postId: Int, userId: Int): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_LIKE_POST_ID, postId)
            put(COLUMN_LIKE_USER_ID, userId)
        }
        return db.insert(TABLE_LIKES, null, values)
    }

    // Retirer un like
    fun removeLike(postId: Int, userId: Int): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_LIKES,
            "$COLUMN_LIKE_POST_ID = ? AND $COLUMN_LIKE_USER_ID = ?",
            arrayOf(postId.toString(), userId.toString())
        )
    }

    // Basculer like/unlike, retourne true si maintenant liké
    fun toggleLike(postId: Int, userId: Int): Boolean {
        if (hasUserLiked(postId, userId)) {
            removeLike(postId, userId)
            return false
        }
        addLike(postId, userId)
        return true
    }

    // Obtenir un any user id existant (utile pour dev bypass)
    fun getAnyUserId(): Int {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USERS,
            arrayOf(COLUMN_ID),
            null, null, null, null, "$COLUMN_ID ASC",
            "1"
        )
        var id = -1
        if (cursor.moveToFirst()) {
            id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID))
        }
        cursor.close()
        return id
    }

    // Récupérer toutes les notifications pour un utilisateur (par défaut tous)
    fun getAllNotifications(recipientUserId: Int? = null): List<UINotification> {
        val notifications = mutableListOf<UINotification>()
        val db = readableDatabase
        val selection: String?
        val selectionArgs: Array<String>?
        if (recipientUserId != null) {
            selection = "$COLUMN_NOTIFICATION_RECIPIENT = ?"
            selectionArgs = arrayOf(recipientUserId.toString())
        } else {
            selection = null
            selectionArgs = null
        }

        val cursor = db.query(
            TABLE_NOTIFICATIONS,
            arrayOf(
                COLUMN_NOTIFICATION_ID,
                COLUMN_NOTIFICATION_ACTOR,
                COLUMN_NOTIFICATION_ACTION,
                COLUMN_NOTIFICATION_LOCATION,
                COLUMN_NOTIFICATION_TIMEAGO,
                COLUMN_NOTIFICATION_PROFILE,
                COLUMN_NOTIFICATION_IS_READ
            ),
            selection,
            selectionArgs,
            null,
            null,
            "$COLUMN_NOTIFICATION_ID DESC"
        )

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ID))
                val actor = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ACTOR))
                val action = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_ACTION))
                val location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_LOCATION))
                val timeAgo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_TIMEAGO))
                val profile = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION_PROFILE))

                val uiNotif = UINotification(
                    id = id,
                    userName = actor ?: "",
                    action = action ?: "",
                    location = location ?: "",
                    timeAgo = timeAgo ?: "",
                    profileImageUrl = profile
                )

                notifications.add(uiNotif)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return notifications
    }

    // Créer une notification
    fun createNotification(recipientUserId: Int, actorName: String, actionText: String, location: String?, profileImageUrl: String?): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOTIFICATION_RECIPIENT, recipientUserId)
            put(COLUMN_NOTIFICATION_ACTOR, actorName)
            put(COLUMN_NOTIFICATION_ACTION, actionText)
            put(COLUMN_NOTIFICATION_LOCATION, location)
            put(COLUMN_NOTIFICATION_TIMEAGO, "à l'instant")
            put(COLUMN_NOTIFICATION_PROFILE, profileImageUrl)
            put(COLUMN_NOTIFICATION_IS_READ, 0)
        }
        return db.insert(TABLE_NOTIFICATIONS, null, values)
    }

    // Marquer comme lu
    fun markNotificationAsRead(notificationId: Int): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NOTIFICATION_IS_READ, 1)
        }
        return db.update(TABLE_NOTIFICATIONS, values, "$COLUMN_NOTIFICATION_ID = ?", arrayOf(notificationId.toString()))
    }
}
