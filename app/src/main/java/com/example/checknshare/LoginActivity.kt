package com.example.checknshare

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checknshare.database.DatabaseHelper
import com.example.checknshare.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser la base de données Supabase
        dbHelper = DatabaseHelper(this)

        // Cacher l'ActionBar pour une meilleure présentation
        supportActionBar?.hide()

        // Bouton de connexion
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                loginUser(email, password)
            }
        }

        // Lien vers l'inscription
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }


    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                binding.etUsername.error = "Email requis"
                false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etUsername.error = "Email invalide"
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Mot de passe requis"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
                false
            }
            else -> true
        }
    }

    private fun loginUser(email: String, password: String) {
        // Désactiver le bouton pendant le chargement
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Connexion en cours..."

        try {
            val user = dbHelper.authenticateUserByEmail(email, password)

            if (user != null) {
                // Connexion réussie
                Toast.makeText(
                    this,
                    "Bienvenue ${user.username} !",
                    Toast.LENGTH_SHORT
                ).show()

                // Sauvegarder la session (SharedPreferences)
                saveUserSession(user.id, user.username, user.email)

                // Naviguer vers MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish() // Fermer LoginActivity
            } else {
                // Échec de connexion
                Toast.makeText(
                    this,
                    "Email ou mot de passe incorrect",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnLogin.isEnabled = true
                binding.btnLogin.text = "Se connecter"
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erreur de connexion: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            binding.btnLogin.isEnabled = true
            binding.btnLogin.text = "Se connecter"
        }
    }

    private fun saveUserSession(userId: Int, username: String, email: String) {
        val sharedPreferences = getSharedPreferences("UserSession", MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putInt("userId", userId)
        editor.putString("username", username)
        editor.putString("email", email)
        editor.putBoolean("isLoggedIn", true)
        editor.apply()
    }


}

