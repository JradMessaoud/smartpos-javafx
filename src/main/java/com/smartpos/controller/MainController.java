package com.smartpos.controller;

import com.smartpos.model.Utilisateur;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {
    
    @FXML private Label userLabel;
    @FXML private Button caisseButton;
    @FXML private Button produitsButton;
    @FXML private Button statsButton;
    @FXML private Button settingsButton;
    @FXML private StackPane contentArea;
    
    private Utilisateur currentUser;
    
    public void initialize(Utilisateur user) {
        this.currentUser = user;
        userLabel.setText("Connecté en tant que : " + user.getLogin());
        
        // Show/hide admin buttons
        boolean isAdmin = user.isAdmin();
        produitsButton.setVisible(isAdmin);
        statsButton.setVisible(isAdmin);
        settingsButton.setVisible(isAdmin);
        
        // Load default view
        if (isAdmin) {
            showProduits();
        } else {
            showCaisse();
        }
    }
    
    @FXML
    private void showCaisse() {
        loadView("/views/Caisse.fxml");
        updateButtonStates(caisseButton);
    }
    
    @FXML
    private void showProduits() {
        loadView("/views/Produits.fxml");
        updateButtonStates(produitsButton);
    }
    
    @FXML
    private void showStats() {
        loadView("/views/Stats.fxml");
        updateButtonStates(statsButton);
    }
    
    @FXML
    private void showSettings() {
        loadView("/views/Settings.fxml");
        updateButtonStates(settingsButton);
    }
    
    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/Login.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) userLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/style.css").toExternalForm());
            
            stage.setScene(scene);
            stage.setTitle("SmartPOS - Login");
            stage.setMaximized(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void loadView(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = loader.load();
            
            // Initialize the controller if it implements Initializable
            if (loader.getController() instanceof Initializable) {
                ((Initializable) loader.getController()).initialize(currentUser);
            }
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void updateButtonStates(Button activeButton) {
        caisseButton.getStyleClass().remove("active");
        produitsButton.getStyleClass().remove("active");
        statsButton.getStyleClass().remove("active");
        settingsButton.getStyleClass().remove("active");
        
        activeButton.getStyleClass().add("active");
    }
    
    // Interface for controllers that need user initialization
    public interface Initializable {
        void initialize(Utilisateur user);
    }
} 