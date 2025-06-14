package com.smartpos.controller;

import com.smartpos.dao.UtilisateurDAO;
import com.smartpos.model.Utilisateur;
import com.smartpos.util.Logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class LoginController {
    
    @FXML private TextField loginField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    
    private UtilisateurDAO utilisateurDAO;
    
    public LoginController() {
        this.utilisateurDAO = new UtilisateurDAO();
    }
    
    @FXML
    private void handleLogin() {
        String login = loginField.getText();
        String password = passwordField.getText();
        
        Logger.info("Tentative de connexion pour l'utilisateur: " + login);
        
        if (login.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Veuillez remplir tous les champs");
            return;
        }
        
        try {
            Utilisateur user = utilisateurDAO.findByLogin(login);
            if (user == null) {
                Logger.warn("Utilisateur non trouvé: " + login);
                errorLabel.setText("Identifiants incorrects");
                return;
            }
            
            Logger.info("Utilisateur trouvé, vérification du mot de passe");
            if (utilisateurDAO.verifierMotDePasse(login, password)) {
                Logger.info("Connexion réussie pour: " + login);
                loadMainInterface(user);
            } else {
                Logger.warn("Mot de passe incorrect pour: " + login);
                errorLabel.setText("Identifiants incorrects");
            }
        } catch (SQLException e) {
            Logger.error("Erreur lors de la connexion", e);
            errorLabel.setText("Erreur de connexion à la base de données");
        }
    }
    
    private void loadMainInterface(Utilisateur user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Main.fxml"));
            Parent root = loader.load();
            
            MainController mainController = loader.getController();
            mainController.initialize(user);
            
            Stage stage = (Stage) loginField.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("SmartPOS - " + (user.isAdmin() ? "Administration" : "Caisse"));
            stage.setMaximized(true);
        } catch (IOException e) {
            Logger.error("Erreur lors du chargement de l'interface", e);
            errorLabel.setText("Erreur lors du chargement de l'interface");
        }
    }
} 