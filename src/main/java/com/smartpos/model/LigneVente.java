package com.smartpos.model;

public class LigneVente {
    private int id;
    private int venteId;
    private int produitId;
    private int quantite;
    private double prixUnitaire;
    private Produit produit;

    public LigneVente() {}

    public LigneVente(int id, int venteId, int produitId, int quantite, double prixUnitaire) {
        this.id = id;
        this.venteId = venteId;
        this.produitId = produitId;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getVenteId() {
        return venteId;
    }

    public void setVenteId(int venteId) {
        this.venteId = venteId;
    }

    public int getProduitId() {
        return produitId;
    }

    public void setProduitId(int produitId) {
        this.produitId = produitId;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public double getPrixUnitaire() {
        return prixUnitaire;
    }

    public void setPrixUnitaire(double prixUnitaire) {
        this.prixUnitaire = prixUnitaire;
    }

    public Produit getProduit() {
        return produit;
    }

    public void setProduit(Produit produit) {
        this.produit = produit;
        if (produit != null) {
            this.produitId = produit.getId();
            this.prixUnitaire = produit.getPrix();
        }
    }

    public double getSousTotal() {
        return quantite * prixUnitaire;
    }
} 