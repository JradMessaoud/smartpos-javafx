package com.smartpos.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.smartpos.dao.ProduitDAO;
import com.smartpos.model.Produit;
import com.smartpos.model.Utilisateur;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.util.Optional;

import javafx.embed.swing.JFXPanel;
import javax.swing.SwingUtilities;

import com.smartpos.util.Validation;
import javafx.geometry.Insets;
import javafx.stage.Modality;

public class ProduitsController implements MainController.Initializable {
    
    @FXML private TextField searchField;
    @FXML private ComboBox<String> categoryFilter;
    @FXML private TableView<Produit> productsTable;
    @FXML private TableColumn<Produit, String> nameColumn;
    @FXML private TableColumn<Produit, Double> priceColumn;
    @FXML private TableColumn<Produit, String> barcodeColumn;
    @FXML private TableColumn<Produit, Integer> stockColumn;
    @FXML private TableColumn<Produit, String> categoryColumn;
    @FXML private TableColumn<Produit, Integer> alertColumn;
    @FXML private TableColumn<Produit, Void> actionsColumn;
    
    private final ProduitDAO produitDAO;
    private final ObservableList<Produit> products = FXCollections.observableArrayList();
    private final FilteredList<Produit> filteredProducts;
    
    // Declare frame and scanThread as class fields
    private JFrame frame;
    private Thread scanThread;
    
    public ProduitsController() {
        this.produitDAO = new ProduitDAO();
        this.filteredProducts = new FilteredList<>(products);
    }
    
    @Override
    public void initialize(Utilisateur user) {
        setupTable();
        setupFilters();
        loadProducts();
    }
    
    private void setupTable() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        barcodeColumn.setCellValueFactory(new PropertyValueFactory<>("codeBarres"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("categorie"));
        alertColumn.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        
        // Setup actions column
        actionsColumn.setCellFactory(createActionButtons());
        
        productsTable.setItems(filteredProducts);
    }
    
    private Callback<TableColumn<Produit, Void>, TableCell<Produit, Void>> createActionButtons() {
        return new Callback<>() {
            @Override
            public TableCell<Produit, Void> call(TableColumn<Produit, Void> param) {
                return new TableCell<>() {
                    private final Button editButton = new Button("Modifier");
                    private final Button deleteButton = new Button("Supprimer");
                    
                    {
                        editButton.getStyleClass().add("button");
                        deleteButton.getStyleClass().add("button");
                        
                        editButton.setOnAction(event -> {
                            Produit produit = getTableView().getItems().get(getIndex());
                            showEditDialog(produit);
                        });
                        
                        deleteButton.setOnAction(event -> {
                            Produit produit = getTableView().getItems().get(getIndex());
                            handleDelete(produit);
                        });
                    }
                    
                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                        } else {
                            HBox buttons = new HBox(5, editButton, deleteButton);
                            setGraphic(buttons);
                        }
                    }
                };
            }
        };
    }
    
    private void setupFilters() {
        // Search filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filteredProducts.setPredicate(produit -> {
                if (newVal == null || newVal.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newVal.toLowerCase();
                return produit.getNom().toLowerCase().contains(lowerCaseFilter) ||
                       produit.getCodeBarres().toLowerCase().contains(lowerCaseFilter);
            });
        });
        
        // Category filter
        categoryFilter.getItems().add("Toutes");
        categoryFilter.setValue("Toutes");
        categoryFilter.setOnAction(e -> {
            String selected = categoryFilter.getValue();
            filteredProducts.setPredicate(produit -> {
                if ("Toutes".equals(selected)) {
                    return true;
                }
                return selected.equals(produit.getCategorie());
            });
        });
    }
    
    private void loadProducts() {
        try {
            products.clear();
            products.addAll(produitDAO.findAll());
            
            // Update category filter
            categoryFilter.getItems().clear();
            categoryFilter.getItems().add("Toutes");
            products.stream()
                   .map(Produit::getCategorie)
                   .distinct()
                   .filter(cat -> cat != null && !cat.isEmpty())
                   .forEach(cat -> categoryFilter.getItems().add(cat));
            
        } catch (SQLException e) {
            showError("Erreur lors du chargement des produits", e);
        }
    }
    
    @FXML
    private void handleAdd() {
        showEditDialog(new Produit());
    }
    
    private void showEditDialog(Produit produit) {
        // Create the custom dialog
        Dialog<Produit> dialog = new Dialog<>();
        dialog.setTitle(produit.getId() == 0 ? "Nouveau Produit" : "Modifier Produit");
        dialog.setHeaderText(null);
        
        // Set the button types
        ButtonType saveButtonType = new ButtonType("Enregistrer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);
        
        // Create the grid for the dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
        
        // Create the input fields
        TextField nameField = new TextField(produit.getNom());
        TextField priceField = new TextField(String.valueOf(produit.getPrix()));
        TextField barcodeField = new TextField(produit.getCodeBarres());
        TextField stockField = new TextField(String.valueOf(produit.getQuantite()));
        ComboBox<String> categoryField = new ComboBox<>();
        categoryField.getItems().addAll("Alimentation", "Boissons", "Électronique", "Vêtements", "Autres");
        categoryField.setValue(produit.getCategorie());
        TextField alertField = new TextField(String.valueOf(produit.getSeuilAlerte()));
        
        // Add fields to grid
        grid.add(new Label("Nom:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Prix:"), 0, 1);
        grid.add(priceField, 1, 1);
        grid.add(new Label("Code-barres:"), 0, 2);
        grid.add(barcodeField, 1, 2);
        grid.add(new Label("Stock:"), 0, 3);
        grid.add(stockField, 1, 3);
        grid.add(new Label("Catégorie:"), 0, 4);
        grid.add(categoryField, 1, 4);
        grid.add(new Label("Seuil d'alerte:"), 0, 5);
        grid.add(alertField, 1, 5);
        
        // Add scan button for barcode
        Button scanButton = new Button("Scanner");
        scanButton.setOnAction(e -> handleScan(barcodeField));
        grid.add(scanButton, 2, 2);
        
        // Enable/Disable save button depending on whether all data was entered
        Button saveButton = (Button) dialog.getDialogPane().lookupButton(saveButtonType);
        saveButton.setDisable(true);
        
        // Do some validation
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            saveButton.setDisable(newVal.trim().isEmpty());
        });
        
        dialog.getDialogPane().setContent(grid);
        
        // Request focus on the name field by default
        Platform.runLater(nameField::requestFocus);
        
        // Convert the result to a Produit when the save button is clicked
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                try {
                    produit.setNom(nameField.getText());
                    produit.setPrix(Double.parseDouble(priceField.getText()));
                    produit.setCodeBarres(barcodeField.getText());
                    produit.setQuantite(Integer.parseInt(stockField.getText()));
                    produit.setCategorie(categoryField.getValue());
                    produit.setSeuilAlerte(Integer.parseInt(alertField.getText()));
                    return produit;
                } catch (NumberFormatException e) {
                    showError("Valeurs numériques invalides", e);
                    return null;
                }
            }
            return null;
        });
        
        Optional<Produit> result = dialog.showAndWait();
        
        result.ifPresent(updatedProduit -> {
            try {
                if (updatedProduit.getId() == 0) {
                    produitDAO.save(updatedProduit);
                } else {
                    produitDAO.update(updatedProduit);
                }
                loadProducts();
            } catch (SQLException e) {
                showError("Erreur lors de l'enregistrement du produit", e);
            }
        });
    }
    
    private void handleScan(TextField barcodeField) {
        // Create webcam object
        Webcam webcam = Webcam.getDefault();
        if (webcam == null) {
            showError("Aucune webcam trouvée.", null);
            return;
        }

        // Set the view size before opening the webcam
        webcam.setViewSize(WebcamResolution.VGA.getSize());

        // Create Swing frame and panel on the Swing EDT
        SwingUtilities.invokeLater(() -> {
            frame = new JFrame("Scanner de code-barres");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(true);
            panel.setImageSizeDisplayed(true);
            panel.setMirrored(true);
            panel.setPreferredSize(webcam.getViewSize());

            frame.getContentPane().add(panel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.start();

            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (scanThread != null) {
                        scanThread.interrupt();
                    }
                }
            });
        });

        scanThread = new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            while (webcam.isOpen()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image != null) {
                        LuminanceSource source = new BufferedImageLuminanceSource(image);
                        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

                        try {
                            Result result = new MultiFormatReader().decode(bitmap);
                            String barcode = result.getText();

                            try {
                                Produit existingProduct = produitDAO.findByCodeBarres(barcode);
                                if (existingProduct != null) {
                                    Platform.runLater(() -> {
                                        Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                        alert.setTitle("Produit existant");
                                        alert.setHeaderText(null);
                                        alert.setContentText("Ce produit existe déjà dans la base de données.\n" +
                                            "Nom: " + existingProduct.getNom() + "\n" +
                                            "Prix: " + existingProduct.getPrix() + " €\n" +
                                            "Stock: " + existingProduct.getQuantite());
                                        alert.showAndWait();
                                    });
                                } else {
                                    Platform.runLater(() -> {
                                        barcodeField.setText(barcode);
                                        webcam.close();
                                        SwingUtilities.invokeLater(() -> {
                                            if (frame != null) frame.dispose();
                                        });
                                    });
                                }
                            } catch (SQLException e) {
                                Platform.runLater(() -> {
                                    showError("Erreur lors de la vérification du produit", e);
                                    webcam.close();
                                    SwingUtilities.invokeLater(() -> {
                                        if (frame != null) frame.dispose();
                                    });
                                });
                            }
                        } catch (NotFoundException e) {
                            // No barcode found, continue scanning
                        }
                    }
                    Thread.sleep(50);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (webcam.isOpen()) {
                webcam.close();
            }
        });

        scanThread.start();
    }
    
    private void handleDelete(Produit produit) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmation");
        alert.setHeaderText(null);
        alert.setContentText("Voulez-vous vraiment supprimer ce produit ?");
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                produitDAO.delete(produit.getId());
                loadProducts();
            } catch (SQLException e) {
                showError("Erreur lors de la suppression", e);
            }
        }
    }
    
    private void showError(String message, Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        if (e != null) {
            e.printStackTrace();
        }
        alert.showAndWait();
    }

    @FXML
    private void handleFilter() {
        String searchText = searchField.getText().trim();
        String selectedCategory = categoryFilter.getValue();
        
        filteredProducts.setPredicate(produit -> {
            boolean matchesSearch = searchText.isEmpty() ||
                produit.getNom().toLowerCase().contains(searchText.toLowerCase()) ||
                produit.getCodeBarres().toLowerCase().contains(searchText.toLowerCase());
            
            boolean matchesCategory = "Toutes".equals(selectedCategory) ||
                (selectedCategory != null && selectedCategory.equals(produit.getCategorie()));
            
            return matchesSearch && matchesCategory;
        });
    }

    @FXML
    private void handleEdit() {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            showEditDialog(selectedProduct);
        } else {
            showError("Veuillez sélectionner un produit à modifier", null);
        }
    }

    @FXML
    private void handleDelete() {
        Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
        if (selectedProduct != null) {
            handleDelete(selectedProduct);
        } else {
            showError("Veuillez sélectionner un produit à supprimer", null);
        }
    }
} 