-- Script de création de la base de données PostgreSQL
SELECT id, username, email, created_at FROM users;
-- Afficher les utilisateurs

) ON CONFLICT (username) DO NOTHING;
    'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae'
    'test@example.com',
    'test',
VALUES (
INSERT INTO users (username, email, password)
-- Le mot de passe est hashé avec SHA-256
-- Insérer un utilisateur de test (mot de passe: test123)

CREATE INDEX idx_email ON users(email);
CREATE INDEX idx_username ON users(username);
-- Créer un index sur username pour améliorer les performances

);
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    username VARCHAR(50) UNIQUE NOT NULL,
    id SERIAL PRIMARY KEY,
CREATE TABLE IF NOT EXISTS users (
-- Créer la table users

\c checknshare_db
-- Se connecter à la base de données

CREATE DATABASE checknshare_db;
-- Créer la base de données

-- Pour Check'N'Share

