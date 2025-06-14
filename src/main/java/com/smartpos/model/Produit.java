package com.smartpos.model;

public class Produit {
    private int id;
    private String nom;
    private double prix;
    private String codeBarres;
    private int quantite;
    private String categorie;
    private int seuilAlerte;

    public Produit() {
        this.seuilAlerte = 5; // Default value
    }

    public Produit(int id, String nom, double prix, String codeBarres, int quantite, String categorie, int seuilAlerte) {
        this.id = id;
        this.nom = nom;
        this.prix = prix;
        this.codeBarres = codeBarres;
        this.quantite = quantite;
        this.categorie = categorie;
        this.seuilAlerte = seuilAlerte;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public double getPrix() {
        return prix;
    }

    public void setPrix(double prix) {
        this.prix = prix;
    }

    public String getCodeBarres() {
        return codeBarres;
    }

    public void setCodeBarres(String codeBarres) {
        this.codeBarres = codeBarres;
    }

    public int getQuantite() {
        return quantite;
    }

    public void setQuantite(int quantite) {
        this.quantite = quantite;
    }

    public String getCategorie() {
        return categorie;
    }

    public void setCategorie(String categorie) {
        this.categorie = categorie;
    }

    public int getSeuilAlerte() {
        return seuilAlerte;
    }

    public void setSeuilAlerte(int seuilAlerte) {
        this.seuilAlerte = seuilAlerte;
    }

    public boolean estStockFaible() {
        return quantite <= seuilAlerte;
    }

    @Override
    public String toString() {
        return nom + " (" + codeBarres + ")";
    }
} 