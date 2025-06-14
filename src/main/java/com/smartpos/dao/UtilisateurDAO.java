package com.smartpos.dao;

import com.smartpos.model.Utilisateur;
import com.smartpos.util.Database;
import com.smartpos.util.Logger;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDAO {
    
    public Utilisateur findByLogin(String login) throws SQLException {
        String sql = "SELECT * FROM utilisateurs WHERE login = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, login);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToUtilisateur(rs);
                }
            }
        }
        
        return null;
    }
    
    public boolean verifierMotDePasse(String login, String motDePasse) throws SQLException {
        Utilisateur user = findByLogin(login);
        if (user == null) {
            Logger.warn("Utilisateur non trouvé pour la vérification du mot de passe: " + login);
            return false;
        }
        
        String hash = hashPassword(motDePasse.trim());
        Logger.info("Hash du mot de passe fourni: " + hash);
        Logger.info("Hash stocké dans la base: " + user.getPassword());
        
        boolean result = hash.equals(user.getPassword());
        Logger.info("Résultat de la vérification: " + result);
        
        return result;
    }
    
    public void changerMotDePasse(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE utilisateurs SET password = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = hashPassword(newPassword.trim());
            Logger.info("Nouveau hash de mot de passe: " + hashedPassword);
            
            stmt.setString(1, hashedPassword);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }
    
    public void creerUtilisateur(String login, String password, String role) throws SQLException {
        String sql = "INSERT INTO utilisateurs (login, password, role) VALUES (?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            String hashedPassword = hashPassword(password.trim());
            Logger.info("Hash du mot de passe pour le nouvel utilisateur: " + hashedPassword);
            
            stmt.setString(1, login);
            stmt.setString(2, hashedPassword);
            stmt.setString(3, role);
            stmt.executeUpdate();
        }
    }
    
    public void creerAdminParDefaut() throws SQLException {
        // Check if admin user already exists
        if (findByLogin("admin") == null) {
            Logger.info("Création de l'utilisateur admin par défaut");
            creerUtilisateur("admin", "admin123", "admin");
        } else {
            Logger.info("L'utilisateur admin existe déjà");
        }
    }
    
    public List<Utilisateur> findAll() throws SQLException {
        List<Utilisateur> users = new ArrayList<>();
        String sql = "SELECT * FROM utilisateurs";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(mapResultSetToUtilisateur(rs));
            }
        }
        
        return users;
    }
    
    public void delete(int userId) throws SQLException {
        String sql = "DELETE FROM utilisateurs WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, userId);
            stmt.executeUpdate();
        }
    }
    
    private Utilisateur mapResultSetToUtilisateur(ResultSet rs) throws SQLException {
        Utilisateur user = new Utilisateur();
        user.setId(rs.getInt("id"));
        user.setLogin(rs.getString("login"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));
        return user;
    }
    
    private String hashPassword(String password) {
        try {
            // Normalize the password by trimming and converting to lowercase
            String normalizedPassword = password.trim().toLowerCase();
            
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(normalizedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Logger.error("Erreur lors du hachage du mot de passe", e);
            throw new RuntimeException("Erreur lors du hachage du mot de passe", e);
        }
    }
} 