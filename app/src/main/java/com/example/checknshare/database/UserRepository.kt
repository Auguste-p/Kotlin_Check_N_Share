package com.example.checknshare.database

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.SQLException
import java.security.MessageDigest

data class User(
    val id: Int,
    val username: String,
    val email: String
)

class UserRepository {
    private val dbHelper = DatabaseHelper()

    companion object {
        private const val TAG = "UserRepository"
    }

    // Hasher le mot de passe
    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    // Authentifier un utilisateur
    suspend fun authenticateUser(username: String, password: String): User? = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        try {
            connection = dbHelper.getConnection()
            connection?.let {
                val hashedPassword = hashPassword(password)
                val query = "SELECT id, username, email FROM users WHERE username = ? AND password = ?"
                val preparedStatement = it.prepareStatement(query)
                preparedStatement.setString(1, username)
                preparedStatement.setString(2, hashedPassword)

                val resultSet = preparedStatement.executeQuery()

                if (resultSet.next()) {
                    val user = User(
                        id = resultSet.getInt("id"),
                        username = resultSet.getString("username"),
                        email = resultSet.getString("email")
                    )
                    resultSet.close()
                    preparedStatement.close()
                    Log.d(TAG, "Utilisateur authentifié: ${user.username}")
                    user
                } else {
                    resultSet.close()
                    preparedStatement.close()
                    Log.d(TAG, "Échec de l'authentification")
                    null
                }
            }
        } catch (e: SQLException) {
            Log.e(TAG, "Erreur lors de l'authentification", e)
            null
        } finally {
            connection?.close()
        }
    }

    // Créer un nouvel utilisateur
    suspend fun createUser(username: String, email: String, password: String): Boolean = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        try {
            connection = dbHelper.getConnection()
            connection?.let {
                val hashedPassword = hashPassword(password)
                val query = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)"
                val preparedStatement = it.prepareStatement(query)
                preparedStatement.setString(1, username)
                preparedStatement.setString(2, email)
                preparedStatement.setString(3, hashedPassword)

                val rowsInserted = preparedStatement.executeUpdate()
                preparedStatement.close()

                if (rowsInserted > 0) {
                    Log.d(TAG, "Utilisateur créé: $username")
                    true
                } else {
                    false
                }
            } ?: false
        } catch (e: SQLException) {
            Log.e(TAG, "Erreur lors de la création de l'utilisateur", e)
            false
        } finally {
            connection?.close()
        }
    }

    // Vérifier si un utilisateur existe
    suspend fun userExists(username: String): Boolean = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        try {
            connection = dbHelper.getConnection()
            connection?.let {
                val query = "SELECT COUNT(*) FROM users WHERE username = ?"
                val preparedStatement = it.prepareStatement(query)
                preparedStatement.setString(1, username)

                val resultSet = preparedStatement.executeQuery()
                resultSet.next()
                val count = resultSet.getInt(1)

                resultSet.close()
                preparedStatement.close()

                count > 0
            } ?: false
        } catch (e: SQLException) {
            Log.e(TAG, "Erreur lors de la vérification de l'utilisateur", e)
            false
        } finally {
            connection?.close()
        }
    }
}

