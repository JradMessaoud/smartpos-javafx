package com.smartpos.controller;

import com.smartpos.dao.SettingsDAO;
import com.smartpos.dao.UtilisateurDAO;
import com.smartpos.model.Utilisateur;
import com.smartpos.util.BackupManager;
import com.smartpos.util.Logger;
import com.smartpos.util.Validation;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.print.Printer;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SettingsController implements MainController.Initializable {
    
    @FXML private TableView<Utilisateur> usersTable;
    @FXML private TableColumn<Utilisateur, String> loginColumn;
    @FXML private TableColumn<Utilisateur, String> roleColumn;
    @FXML private TableColumn<Utilisateur, Void> userActionsColumn;
    
    @FXML private TextField storeNameField;
    @FXML private TextField storeAddressField;
    @FXML private TextField storePhoneField;
    @FXML private TextField storeEmailField;
    
    @FXML private TextArea receiptHeaderField;
    @FXML private TextArea receiptFooterField;
    @FXML private ComboBox<String> printerComboBox;
    
    private UtilisateurDAO utilisateurDAO;
    private SettingsDAO settingsDAO;
    private Utilisateur currentUser;
    
    public SettingsController() {
        this.utilisateurDAO = new UtilisateurDAO();
        this.settingsDAO = new SettingsDAO();
    }
    
    @Override
    public void initialize(Utilisateur user) {
        this.currentUser = user;
        setupUserTable();
        loadUsers();
        loadSettings();
        setupPrinters();
        Logger.info("Settings interface initialized for user: " + user.getLogin());
    }
    
    private void setupUserTable() {
        loginColumn.setCellValueFactory(new PropertyValueFactory<>("login"));
        roleColumn.setCellValueFactory(new PropertyValueFactory<>("role"));
        
        // Setup actions column
        userActionsColumn.setCellFactory(createUserActionButtons());
    }
    
    private Callback<TableColumn<Utilisateur, Void>, TableCell<Utilisateur, Void>> createUserActionButtons() {
        return new Callback<>() {
            @Override
            public TableCell<Utilisateur, Void> call(TableColumn<Utilisateur, Void> param) {
                return new TableCell<>() {
                    private final Button deleteButton = new Button("Supprimer");
                    
                    {
                        deleteButton.getStyleClass().add("button");
                        deleteButton.setOnAction(event -> {
                            Utilisateur user = getTableView().getItems().get(getIndex());
                            handleDeleteUser(user);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            Utilisateur user = getTableView().getItems().get(getIndex());
                            // Don't allow deleting the current user
                            deleteButton.setDisable(user.getId() == currentUser.getId());
                            setGraphic(deleteButton);
                        }
                    }
                };
            }
        };
    }
    
    private void loadUsers() {
        try {
            usersTable.setItems(FXCollections.observableArrayList(utilisateurDAO.findAll()));
            Logger.info("Loaded users list");
        } catch (SQLException e) {
            Logger.error("Error loading users", e);
            showError("Erreur lors du chargement des utilisateurs", e);
        }
    }
    
    private void loadSettings() {
        try {
            Map<String, String> settings = settingsDAO.getAllSettings();
            
            storeNameField.setText(settings.get("nom_magasin"));
            storeAddressField.setText(settings.get("adresse"));
            storePhoneField.setText(settings.get("telephone"));
            storeEmailField.setText(settings.get("email"));
            receiptHeaderField.setText(settings.get("en_tete_ticket"));
            receiptFooterField.setText(settings.get("pied_ticket"));
            printerComboBox.setValue(settings.get("imprimante"));
            
            Logger.info("Loaded application settings");
        } catch (Exception e) {
            Logger.error("Error loading settings", e);
            showError("Erreur lors du chargement des paramètres", e);
        }
    }
    
    private void setupPrinters() {
        printerComboBox.getItems().clear();
        for (Printer printer : Printer.getAllPrinters()) {
            printerComboBox.getItems().add(printer.getName());
        }
        
        if (printerComboBox.getItems().isEmpty()) {
            printerComboBox.getItems().add("Aucune imprimante détectée");
        }
        
        printerComboBox.setValue(Printer.getDefaultPrinter() != null ? 
            Printer.getDefaultPrinter().getName() : "Aucune imprimante détectée");
        
        Logger.info("Setup printers: " + printerComboBox.getItems().size() + " printers found");
    }
    
    @FXML
    private void handleAddUser() {
        // Create the dialog
        Dialog<Utilisateur> dialog = new Dialog<>();
        dialog.setTitle("Ajouter un utilisateur");
        dialog.setHeaderText(null);
    
        // Set the button types
        ButtonType addButtonType = new ButtonType("Ajouter", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);
    
        // Create the login, password, and role fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
    
        TextField loginField = new TextField();
        loginField.setPromptText("Login");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Mot de passe");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("admin", "caissier");
        roleComboBox.setValue("caissier");
    
        grid.add(new Label("Login:"), 0, 0);
        grid.add(loginField, 1, 0);
        grid.add(new Label("Mot de passe:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Rôle:"), 0, 2);
        grid.add(roleComboBox, 1, 2);
    
        dialog.getDialogPane().setContent(grid);
    
        // Convert the result to a user object
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                return new Utilisateur(0, loginField.getText(), passwordField.getText(), roleComboBox.getValue());
            }
            return null;
        });
    
        Optional<Utilisateur> result = dialog.showAndWait();
    
        result.ifPresent(newUser -> {
            try {
                String login = Validation.sanitizeInput(newUser.getLogin());
                String password = newUser.getPassword();
                String role = newUser.getRole();
    
                if (!Validation.isValidLogin(login)) {
                    showError("L'identifiant doit contenir au moins 3 caractères alphanumériques", null);
                    return;
                }
    
                if (!Validation.isValidPassword(password)) {
                    showError("Le mot de passe doit contenir au moins 6 caractères", null);
                    return;
                }
    
                utilisateurDAO.creerUtilisateur(login, password, role);
                loadUsers();
                Logger.info("Created new user: " + login);
                showInfo("Utilisateur créé avec succès");
    
            } catch (SQLException e) {
                Logger.error("Error creating user", e);
                showError("Erreur lors de la création de l'utilisateur: " + e.getMessage(), e);
            }
        });
    }
    
    @FXML
    private void handleChangePassword() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur", null);
            return;
        }
        
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Changer le mot de passe");
        dialog.setHeaderText(null);
        
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Nouveau mot de passe");
        dialogPane.setContent(passwordField);
        
        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return passwordField.getText();
            }
            return null;
        });
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(password -> {
            try {
                if (!Validation.isValidPassword(password)) {
                    showError("Le mot de passe doit contenir au moins 6 caractères", null);
                    return;
                }
                
                utilisateurDAO.changerMotDePasse(selectedUser.getId(), password);
                Logger.info("Changed password for user: " + selectedUser.getLogin());
                showInfo("Mot de passe modifié avec succès");
            } catch (SQLException e) {
                Logger.error("Error changing password", e);
                showError("Erreur lors du changement de mot de passe", e);
            }
        });
    }
    
    private void handleDeleteUser(Utilisateur user) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer cet utilisateur ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                utilisateurDAO.delete(user.getId());
                loadUsers();
                Logger.info("Deleted user: " + user.getLogin());
            } catch (Exception e) {
                Logger.error("Error deleting user", e);
                showError("Erreur lors de la suppression", e);
            }
        }
    }
    
    @FXML
    private void handleDeleteUser() {
        Utilisateur selectedUser = usersTable.getSelectionModel().getSelectedItem();
        if (selectedUser == null) {
            showError("Veuillez sélectionner un utilisateur", null);
            return;
        }
        // Call the private handleDeleteUser with the selected user
        handleDeleteUser(selectedUser);
    }
    
    @FXML
    private void handleSave() {
        try {
            Map<String, String> settings = new HashMap<>();
            settings.put("nom_magasin", storeNameField.getText());
            settings.put("adresse", storeAddressField.getText());
            settings.put("telephone", storePhoneField.getText());
            settings.put("email", storeEmailField.getText());
            settings.put("en_tete_ticket", receiptHeaderField.getText());
            settings.put("pied_ticket", receiptFooterField.getText());
            settings.put("imprimante", printerComboBox.getValue());
            
            settingsDAO.updateSettings(settings);
            Logger.info("Settings saved");
            showInfo("Paramètres enregistrés avec succès");
        } catch (SQLException e) {
            Logger.error("Error saving settings", e);
            showError("Erreur lors de l'enregistrement des paramètres", e);
        }
    }
    
    @FXML
    private void handleBackup() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un emplacement de sauvegarde");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Files", "*.db"));
        
        // Set initial directory to user's documents or home directory
        File initialDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDirectory);
        
        File file = fileChooser.showSaveDialog(null);
        
        if (file != null) {
            try {
                BackupManager.createBackup(file.getAbsolutePath());
                Logger.info("Database backup created at: " + file.getAbsolutePath());
                showInfo("Sauvegarde créée avec succès");
            } catch (IOException | SQLException e) {
                Logger.error("Error creating backup", e);
                showError("Erreur lors de la création de la sauvegarde", e);
            }
        }
    }
    
    @FXML
    private void handleRestore() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir un fichier de sauvegarde");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Database Files", "*.db"));

        // Set initial directory to user's documents or home directory
        File initialDirectory = new File(System.getProperty("user.home"));
        fileChooser.setInitialDirectory(initialDirectory);

        File file = fileChooser.showOpenDialog(null);
        
        if (file != null) {
            try {
                // Confirmation dialog
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation de restauration");
                alert.setHeaderText("Cette action remplacera la base de données actuelle.");
                alert.setContentText("Êtes-vous sûr de vouloir continuer ?");
                
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    BackupManager.restoreDatabase(file.getAbsolutePath());
                    Logger.info("Database restored from: " + file.getAbsolutePath());
                    showInfo("Restauration réussie. Veuillez redémarrer l'application.");
                    // Consider restarting the application or reloading necessary data
                }
            } catch (IOException | SQLException e) {
                Logger.error("Error restoring database", e);
                showError("Erreur lors de la restauration de la base de données", e);
            }
        }
    }
    
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (e != null) {
            Logger.error("Showing error: " + message, e);
        } else {
             Logger.error("Showing error: " + message);
        }
        alert.showAndWait();
    }
    
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        Logger.info("Showing info: " + message);
        alert.showAndWait();
    }
} 