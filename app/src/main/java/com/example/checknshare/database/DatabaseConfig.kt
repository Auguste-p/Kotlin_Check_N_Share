package com.example.checknshare.database

import com.example.checknshare.BuildConfig

object DatabaseConfig {
    // Configuration Supabase (chargée depuis local.properties)
    val SUPABASE_URL: String
        get() = BuildConfig.SUPABASE_URL

    val SUPABASE_API_KEY: String
        get() = BuildConfig.SUPABASE_API_KEY

    // Ancienne configuration PostgreSQL locale (deprecated)
    @Deprecated("Use Supabase instead")
    const val DB_HOST = "10.0.2.2"
    @Deprecated("Use Supabase instead")
    const val DB_PORT = "5432"
    @Deprecated("Use Supabase instead")
    const val DB_NAME = "checknshare_db"
    @Deprecated("Use Supabase instead")
    const val DB_USER = "postgres"
    @Deprecated("Use Supabase instead")
    const val DB_PASSWORD = "azertyuiop"
    @Deprecated("Use Supabase instead")
    val DB_URL = "jdbc:postgresql://$DB_HOST:$DB_PORT/$DB_NAME"
}


