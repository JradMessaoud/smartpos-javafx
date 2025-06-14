package com.smartpos.dao;

import com.smartpos.model.Produit;
import com.smartpos.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProduitDAO {
    
    public List<Produit> findAll() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits ORDER BY nom";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }
    
    public List<Produit> findByCategorie(String categorie) throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE categorie = ? ORDER BY nom";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, categorie);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }
    
    public List<Produit> findStockFaible() throws SQLException {
        List<Produit> produits = new ArrayList<>();
        String sql = "SELECT * FROM produits WHERE stock <= seuil_alerte ORDER BY nom";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                produits.add(mapResultSetToProduit(rs));
            }
        }
        return produits;
    }
    
    public Produit findById(int id) throws SQLException {
        String sql = "SELECT * FROM produits WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        }
        return null;
    }
    
    public Produit findByCodeBarres(String codeBarres) throws SQLException {
        String sql = "SELECT * FROM produits WHERE code_barres = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, codeBarres);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return mapResultSetToProduit(rs);
            }
        }
        return null;
    }
    
    public void save(Produit produit) throws SQLException {
        if (produit.getId() == 0) {
            insert(produit);
        } else {
            update(produit);
        }
    }
    
    private void insert(Produit produit) throws SQLException {
        String sql = "INSERT INTO produits (nom, prix, code_barres, stock, categorie, seuil_alerte) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, produit.getNom());
            stmt.setDouble(2, produit.getPrix());
            stmt.setString(3, produit.getCodeBarres());
            stmt.setInt(4, produit.getQuantite());
            stmt.setString(5, produit.getCategorie());
            stmt.setInt(6, produit.getSeuilAlerte());
            stmt.executeUpdate();
        }
    }
    
    public void update(Produit produit) throws SQLException {
        String sql = "UPDATE produits SET nom = ?, prix = ?, code_barres = ?, " +
                    "stock = ?, categorie = ?, seuil_alerte = ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, produit.getNom());
            stmt.setDouble(2, produit.getPrix());
            stmt.setString(3, produit.getCodeBarres());
            stmt.setInt(4, produit.getQuantite());
            stmt.setString(5, produit.getCategorie());
            stmt.setInt(6, produit.getSeuilAlerte());
            stmt.setInt(7, produit.getId());
            stmt.executeUpdate();
        }
    }
    
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM produits WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    
    public void updateStock(int id, int quantite) throws SQLException {
        String sql = "UPDATE produits SET stock = stock + ? WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, quantite);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }
    
    private Produit mapResultSetToProduit(ResultSet rs) throws SQLException {
        return new Produit(
            rs.getInt("id"),
            rs.getString("nom"),
            rs.getDouble("prix"),
            rs.getString("code_barres"),
            rs.getInt("stock"),
            rs.getString("categorie"),
            rs.getInt("seuil_alerte")
        );
    }
} 