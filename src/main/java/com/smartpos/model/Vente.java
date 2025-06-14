package com.smartpos.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Vente {
    private int id;
    private LocalDateTime date;
    private double total;
    private double remise;
    private int utilisateurId;
    private List<LigneVente> lignes;

    public Vente() {
        this.date = LocalDateTime.now();
        this.lignes = new ArrayList<>();
        this.remise = 0.0;
    }

    public Vente(int id, LocalDateTime date, double total, double remise, int utilisateurId) {
        this.id = id;
        this.date = date;
        this.total = total;
        this.remise = remise;
        this.utilisateurId = utilisateurId;
        this.lignes = new ArrayList<>();
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public double getRemise() {
        return remise;
    }

    public void setRemise(double remise) {
        this.remise = remise;
    }

    public int getUtilisateurId() {
        return utilisateurId;
    }

    public void setUtilisateurId(int utilisateurId) {
        this.utilisateurId = utilisateurId;
    }

    public List<LigneVente> getLignes() {
        return lignes;
    }

    public void setLignes(List<LigneVente> lignes) {
        this.lignes = lignes;
    }

    public void ajouterLigne(LigneVente ligne) {
        this.lignes.add(ligne);
        calculerTotal();
    }

    public void supprimerLigne(LigneVente ligne) {
        this.lignes.remove(ligne);
        calculerTotal();
    }

    public void calculerTotal() {
        this.total = this.lignes.stream()
                .mapToDouble(LigneVente::getSousTotal)
                .sum();
        
        if (this.remise > 0) {
            this.total = this.total * (1 - this.remise / 100);
        }
    }
} 