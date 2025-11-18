package com.example.checknshare.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.security.MessageDigest

class LocalDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "checknshare.db"
        private const val DATABASE_VERSION = 1

        private const val TABLE_USERS = "users"
        private const val COLUMN_ID = "id"
        private const val COLUMN_USERNAME = "username"
        private const val COLUMN_EMAIL = "email"
        private const val COLUMN_PASSWORD = "password"
        private const val COLUMN_CREATED_AT = "created_at"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = """
            CREATE TABLE $TABLE_USERS (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_USERNAME TEXT UNIQUE NOT NULL,
                $COLUMN_EMAIL TEXT UNIQUE NOT NULL,
                $COLUMN_PASSWORD TEXT NOT NULL,
                $COLUMN_CREATED_AT TEXT DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()

        db?.execSQL(createTableQuery)

        // Créer un utilisateur de test (username: test, password: test123)
        val hashedPassword = hashPassword("test123")
        val values = ContentValues().apply {
            put(COLUMN_USERNAME, "test")
            put(COLUMN_EMAIL, "test@example.com")
            put(COLUMN_PASSWORD, hashedPassword)
        }
        db?.insert(TABLE_USERS, null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        onCreate(db)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Authentifier un utilisateur
    fun authenticateUser(username: String, password: String): User? {
        val db = readableDatabase
        val hashedPassword = hashPassword(password)

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
            user
        } else {
            cursor.close()
            null
        }
    }

    // Créer un nouvel utilisateur
    fun createUser(username: String, email: String, password: String): Boolean {
        val db = writableDatabase
        val hashedPassword = hashPassword(password)

        val values = ContentValues().apply {
            put(COLUMN_USERNAME, username)
            put(COLUMN_EMAIL, email)
            put(COLUMN_PASSWORD, hashedPassword)
        }

        return try {
            val result = db.insert(TABLE_USERS, null, values)
            result != -1L
        } catch (e: Exception) {
            false
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
}

