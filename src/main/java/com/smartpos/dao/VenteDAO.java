package com.smartpos.dao;

import com.smartpos.model.LigneVente;
import com.smartpos.model.Vente;
import com.smartpos.util.Database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VenteDAO {
    
    public void save(Vente vente) throws SQLException {
        Connection conn = null;
        try {
            conn = Database.getConnection();
            conn.setAutoCommit(false);
            
            // Insert sale
            String sqlVente = "INSERT INTO ventes (date, montant_total, remise, utilisateur_id) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlVente, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setObject(1, vente.getDate());
                stmt.setDouble(2, vente.getTotal());
                stmt.setDouble(3, vente.getRemise());
                stmt.setInt(4, vente.getUtilisateurId());
                stmt.executeUpdate();
                
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    vente.setId(rs.getInt(1));
                }
            }
            
            // Insert sale lines
            String sqlLigne = "INSERT INTO lignes_vente (vente_id, produit_id, quantite, prix_unitaire) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlLigne)) {
                for (LigneVente ligne : vente.getLignes()) {
                    stmt.setInt(1, vente.getId());
                    stmt.setInt(2, ligne.getProduitId());
                    stmt.setInt(3, ligne.getQuantite());
                    stmt.setDouble(4, ligne.getPrixUnitaire());
                    stmt.executeUpdate();
                    
                    // Update product stock
                    updateProductStock(conn, ligne.getProduitId(), -ligne.getQuantite());
                }
            }
            
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void updateProductStock(Connection conn, int produitId, int quantite) throws SQLException {
        String sql = "UPDATE produits SET stock = stock + ? WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantite);
            stmt.setInt(2, produitId);
            stmt.executeUpdate();
        }
    }
    
    public List<Vente> findByDate(LocalDateTime dateDebut, LocalDateTime dateFin) throws SQLException {
        List<Vente> ventes = new ArrayList<>();
        String sql = "SELECT * FROM ventes WHERE date BETWEEN ? AND ? ORDER BY date DESC";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setObject(1, dateDebut);
            stmt.setObject(2, dateFin);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Vente vente = mapResultSetToVente(rs);
                vente.setLignes(findLignesVente(conn, vente.getId()));
                ventes.add(vente);
            }
        }
        return ventes;
    }
    
    public Vente findById(int id) throws SQLException {
        String sql = "SELECT * FROM ventes WHERE id = ?";
        
        try (Connection conn = Database.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Vente vente = mapResultSetToVente(rs);
                vente.setLignes(findLignesVente(conn, id));
                return vente;
            }
        }
        return null;
    }
    
    private List<LigneVente> findLignesVente(Connection conn, int venteId) throws SQLException {
        List<LigneVente> lignes = new ArrayList<>();
        String sql = "SELECT * FROM lignes_vente WHERE vente_id = ?";
        
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venteId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                lignes.add(mapResultSetToLigneVente(rs));
            }
        }
        return lignes;
    }
    
    private Vente mapResultSetToVente(ResultSet rs) throws SQLException {
        return new Vente(
            rs.getInt("id"),
            rs.getObject("date", LocalDateTime.class),
            rs.getDouble("montant_total"),
            rs.getDouble("remise"),
            rs.getInt("utilisateur_id")
        );
    }
    
    private LigneVente mapResultSetToLigneVente(ResultSet rs) throws SQLException {
        return new LigneVente(
            rs.getInt("id"),
            rs.getInt("vente_id"),
            rs.getInt("produit_id"),
            rs.getInt("quantite"),
            rs.getDouble("prix_unitaire")
        );
    }
} 