package com.smartpos.dao;

import com.smartpos.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SettingsDAO {
    
    public Map<String, String> getAllSettings() throws SQLException {
        Map<String, String> settings = new HashMap<>();
        String sql = "SELECT cle, valeur FROM parametres";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                settings.put(rs.getString("cle"), rs.getString("valeur"));
            }
        }
        
        return settings;
    }
    
    public String getSetting(String key) throws SQLException {
        String sql = "SELECT valeur FROM parametres WHERE cle = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, key);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("valeur");
                }
            }
        }
        
        return null;
    }
    
    public void updateSetting(String key, String value) throws SQLException {
        String sql = "UPDATE parametres SET valeur = ? WHERE cle = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, value);
            stmt.setString(2, key);
            stmt.executeUpdate();
        }
    }
    
    public void updateSettings(Map<String, String> settings) throws SQLException {
        String sql = "UPDATE parametres SET valeur = ? WHERE cle = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                stmt.setString(1, entry.getValue());
                stmt.setString(2, entry.getKey());
                stmt.executeUpdate();
            }
        }
    }
} 