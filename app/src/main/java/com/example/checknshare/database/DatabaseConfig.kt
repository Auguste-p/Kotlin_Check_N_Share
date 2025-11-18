package com.example.checknshare.database

object DatabaseConfig {
    // Configuration de la base de données PostgreSQL locale
    const val DB_HOST = "10.0.2.2" // Pour l'émulateur Android (localhost)
    const val DB_PORT = "5432"
    const val DB_NAME = "checknshare_db"
    const val DB_USER = "postgres"
    const val DB_PASSWORD = "azertyuiop" // À modifier selon votre configuration

    val DB_URL = "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
}


