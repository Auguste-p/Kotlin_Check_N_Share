package com.example.checknshare.database

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class DatabaseHelper {

    companion object {
        private const val TAG = "DatabaseHelper"

        // Charger le driver JDBC
        init {
            try {
                Class.forName("org.postgresql.Driver")
            } catch (e: ClassNotFoundException) {
                Log.e(TAG, "Driver PostgreSQL non trouvé", e)
            }
        }
    }

    // Obtenir une connexion à la base de données
    suspend fun getConnection(): Connection? = withContext(Dispatchers.IO) {
        try {
            DriverManager.getConnection(
                DatabaseConfig.DB_URL,
                DatabaseConfig.DB_USER,
                DatabaseConfig.DB_PASSWORD
            )
        } catch (e: SQLException) {
            Log.e(TAG, "Erreur de connexion à la base de données", e)
            null
        }
    }

    // Créer la table users si elle n'existe pas
    suspend fun createUsersTable() = withContext(Dispatchers.IO) {
        var connection: Connection? = null
        try {
            connection = getConnection()
            connection?.let {
                val statement = it.createStatement()
                val createTableSQL = """
                    CREATE TABLE IF NOT EXISTS users (
                        id SERIAL PRIMARY KEY,
                        username VARCHAR(50) UNIQUE NOT NULL,
                        email VARCHAR(100) UNIQUE NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                    )
                """.trimIndent()

                statement.executeUpdate(createTableSQL)
                statement.close()
                Log.d(TAG, "Table users créée ou déjà existante")
            }
        } catch (e: SQLException) {
            Log.e(TAG, "Erreur lors de la création de la table", e)
        } finally {
            connection?.close()
        }
    }
}

