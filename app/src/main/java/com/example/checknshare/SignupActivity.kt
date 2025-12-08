package com.example.checknshare

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.checknshare.database.DatabaseHelper
import com.example.checknshare.databinding.ActivitySignupBinding

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var dbHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialiser la base de données Supabase
        dbHelper = DatabaseHelper(this)

        // Cacher l'ActionBar pour une meilleure présentation
        supportActionBar?.hide()

        // Bouton d'inscription
        binding.btnSignup.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(username, email, password, confirmPassword)) {
                registerUser(username, email, password)
            }
        }

        // Lien vers la connexion
        binding.tvLogin.setOnClickListener {
            finish() // Retourner à LoginActivity
        }
    }

    private fun validateInput(
        username: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        // Réinitialiser les erreurs
        binding.etUsername.error = null
        binding.etEmail.error = null
        binding.etPassword.error = null
        binding.etConfirmPassword.error = null

        return when {
            username.isEmpty() -> {
                binding.etUsername.error = "Nom d'utilisateur requis"
                binding.etUsername.requestFocus()
                false
            }
            username.length < 3 -> {
                binding.etUsername.error = "Le nom d'utilisateur doit contenir au moins 3 caractères"
                binding.etUsername.requestFocus()
                false
            }
            email.isEmpty() -> {
                binding.etEmail.error = "Email requis"
                binding.etEmail.requestFocus()
                false
            }
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.etEmail.error = "Email invalide"
                binding.etEmail.requestFocus()
                false
            }
            password.isEmpty() -> {
                binding.etPassword.error = "Mot de passe requis"
                binding.etPassword.requestFocus()
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Le mot de passe doit contenir au moins 6 caractères"
                binding.etPassword.requestFocus()
                false
            }
            confirmPassword.isEmpty() -> {
                binding.etConfirmPassword.error = "Confirmation du mot de passe requise"
                binding.etConfirmPassword.requestFocus()
                false
            }
            password != confirmPassword -> {
                binding.etConfirmPassword.error = "Les mots de passe ne correspondent pas"
                binding.etConfirmPassword.requestFocus()
                false
            }
            else -> true
        }
    }

    private fun registerUser(username: String, email: String, password: String) {
        // Désactiver le bouton pendant le chargement
        binding.btnSignup.isEnabled = false
        binding.btnSignup.text = "Inscription en cours..."

        try {
            // Vérifier si l'utilisateur existe déjà
            if (dbHelper.userExists(username)) {
                Toast.makeText(
                    this,
                    "Ce nom d'utilisateur est déjà utilisé",
                    Toast.LENGTH_SHORT
                ).show()
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "S'inscrire"
                return
            }

            // Créer l'utilisateur
            val success = dbHelper.createUser(username, email, password)

            if (success) {
                // Inscription réussie
                Toast.makeText(
                    this,
                    "Compte créé avec succès ! Connectez-vous maintenant.",
                    Toast.LENGTH_LONG
                ).show()

                // Rediriger vers LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                // Échec de l'inscription
                Toast.makeText(
                    this,
                    "Erreur lors de la création du compte. L'email est peut-être déjà utilisé.",
                    Toast.LENGTH_LONG
                ).show()
                binding.btnSignup.isEnabled = true
                binding.btnSignup.text = "S'inscrire"
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Erreur: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
            binding.btnSignup.isEnabled = true
            binding.btnSignup.text = "S'inscrire"
        }
    }
}

