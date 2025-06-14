package com.smartpos.controller;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.smartpos.dao.ProduitDAO;
import com.smartpos.dao.SettingsDAO;
import com.smartpos.dao.VenteDAO;
import com.smartpos.model.LigneVente;
import com.smartpos.model.Produit;
import com.smartpos.model.Utilisateur;
import com.smartpos.model.Vente;
import com.smartpos.util.Logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.print.*;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

public class CaisseController implements MainController.Initializable {
    
    @FXML private TextField barcodeField;
    @FXML private TableView<Produit> productsTable;
    @FXML private TableColumn<Produit, String> nameColumn;
    @FXML private TableColumn<Produit, Double> priceColumn;
    @FXML private TableColumn<Produit, Integer> stockColumn;
    
    @FXML private TableView<LigneVente> cartTable;
    @FXML private TableColumn<LigneVente, String> cartNameColumn;
    @FXML private TableColumn<LigneVente, String> cartCategoryColumn;
    @FXML private TableColumn<LigneVente, Integer> cartQuantityColumn;
    @FXML private TableColumn<LigneVente, Double> cartPriceColumn;
    @FXML private TableColumn<LigneVente, Double> cartTotalColumn;
    
    @FXML private TextField discountField;
    @FXML private Label totalLabel;
    @FXML private TextField amountReceivedField;
    @FXML private Label changeLabel;
    
    private ProduitDAO produitDAO;
    private VenteDAO venteDAO;
    private SettingsDAO settingsDAO;
    private Utilisateur currentUser;
    private Vente currentVente;
    private ObservableList<LigneVente> cartItems;
    private NumberFormat currencyFormat;
    
    public CaisseController() {
        this.produitDAO = new ProduitDAO();
        this.venteDAO = new VenteDAO();
        this.settingsDAO = new SettingsDAO();
        this.cartItems = FXCollections.observableArrayList();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
    }
    
    @Override
    public void initialize(Utilisateur user) {
        this.currentUser = user;
        this.currentVente = new Vente();
        this.currentVente.setUtilisateurId(user.getId());
        
        setupTables();
        loadProducts();
        setupEventHandlers();
    }
    
    private void setupTables() {
        // Products table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("prix"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        
        // Add double-click handler for products table
        productsTable.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Produit selectedProduct = productsTable.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    addToCart(selectedProduct);
                }
            }
        });
        
        // Cart table
        cartNameColumn.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue().getProduit();
            return new SimpleStringProperty(produit != null ? produit.getNom() : "");
        });
        cartCategoryColumn.setCellValueFactory(cellData -> {
            Produit produit = cellData.getValue().getProduit();
            return new SimpleStringProperty(produit != null ? produit.getCategorie() : "");
        });
        cartQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        cartPriceColumn.setCellValueFactory(new PropertyValueFactory<>("prixUnitaire"));
        cartTotalColumn.setCellValueFactory(new PropertyValueFactory<>("sousTotal"));
        
        cartTable.setItems(cartItems);
    }
    
    private void loadProducts() {
        try {
            productsTable.setItems(FXCollections.observableArrayList(produitDAO.findAll()));
        } catch (SQLException e) {
            showError("Erreur lors du chargement des produits", e);
        }
    }
    
    private void setupEventHandlers() {
        // Handle barcode field enter key
        barcodeField.setOnAction(e -> handleBarcodeInput());
        
        // Handle discount field changes
        discountField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateTotal();
        });
        
        // Handle amount received field changes
        amountReceivedField.textProperty().addListener((obs, oldVal, newVal) -> {
            updateTotal();
        });
    }
    
    @FXML
    private void handleScan() {
        try {
            // Create a new stage for the camera view
            Stage cameraStage = new Stage();
            cameraStage.setTitle("Scanner de code-barres");
            
            // Create a video capture component
            Webcam webcam = Webcam.getDefault();
            if (webcam == null) {
                showError("Aucune webcam détectée", null);
                return;
            }
            
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            
            // Create a panel to display the camera feed
            WebcamPanel panel = new WebcamPanel(webcam);
            panel.setPreferredSize(WebcamResolution.VGA.getSize());
            panel.setFPSDisplayed(true);
            panel.setDisplayDebugInfo(true);
            panel.setImageSizeDisplayed(true);
            panel.setMirrored(true);
            
            // Create a frame to hold the panel
            JFrame frame = new JFrame();
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            
            // Start a thread to process the camera feed
            new Thread(() -> {
                while (frame.isVisible()) {
                    try {
                        BufferedImage image = webcam.getImage();
                        if (image != null) {
                            // Process the image with ZXing
                            LuminanceSource source = new BufferedImageLuminanceSource(image);
                            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                            
                            try {
                                Result result = new MultiFormatReader().decode(bitmap);
                                String code = result.getText();
                                
                                // Close the camera window
                                Platform.runLater(() -> {
                                    frame.dispose();
                                    webcam.close();
                                    
                                    // Add the product to cart
                                    try {
                                        Produit produit = produitDAO.findByCodeBarres(code);
                                        if (produit != null) {
                                            addToCart(produit);
                                        } else {
                                            showError("Produit non trouvé", null);
                                        }
                                    } catch (SQLException e) {
                                        showError("Erreur lors de la recherche du produit", e);
                                    }
                                });
                                
                                return;
                            } catch (NotFoundException e) {
                                // No barcode found, continue scanning
                            }
                        }
                    } catch (Exception e) {
                        Logger.error("Error during barcode scanning", e);
                    }
                }
                
                // Clean up
                webcam.close();
            }).start();
            
        } catch (Exception e) {
            Logger.error("Error initializing barcode scanner", e);
            showError("Erreur lors de l'initialisation du scanner", e);
        }
    }
    
    @FXML
    private void handleSearch() {
        String searchText = barcodeField.getText().trim();
        if (!searchText.isEmpty()) {
            try {
                Produit produit = produitDAO.findByCodeBarres(searchText);
                if (produit != null) {
                    addToCart(produit);
                    barcodeField.clear();
                } else {
                    showError("Produit non trouvé", null);
                }
            } catch (SQLException e) {
                showError("Erreur lors de la recherche", e);
            }
        }
    }
    
    private void handleBarcodeInput() {
        handleSearch();
    }
    
    private void addToCart(Produit produit) {
        if (produit.getQuantite() <= 0) {
            showError("Stock insuffisant", null);
            return;
        }
        
        // Check if product is already in cart
        for (LigneVente ligne : cartItems) {
            if (ligne.getProduitId() == produit.getId()) {
                if (ligne.getQuantite() < produit.getQuantite()) {
                    ligne.setQuantite(ligne.getQuantite() + 1);
                    cartTable.refresh();
                    updateTotal();
                } else {
                    showError("Stock insuffisant", null);
                }
                return;
            }
        }
        
        // Add new product to cart
        LigneVente ligne = new LigneVente();
        ligne.setProduit(produit);
        ligne.setQuantite(1);
        cartItems.add(ligne);
        updateTotal();
    }
    
    private void updateTotal() {
        double totalBrut = calculerTotalBrut();
        double totalRemise = calculerTotalApresRemise();
        double monnaie = calculerMonnaieARendre();

        // Affichage du total après remise (total à payer)
        totalLabel.setText(currencyFormat.format(totalRemise));
        // Affichage de la monnaie à rendre
        changeLabel.setText(currencyFormat.format(monnaie));
        // Si tu veux afficher le total brut dans un autre label, décommente la ligne suivante et ajoute un label dans le FXML
        // totalBrutLabel.setText(currencyFormat.format(totalBrut));
        
        // Mettre à jour le total dans la vente pour la sauvegarde
        currentVente.setTotal(totalRemise);
    }
    
    private double calculerTotalBrut() {
        return cartItems.stream()
            .mapToDouble(LigneVente::getSousTotal)
            .sum();
    }

    private double calculerTotalApresRemise() {
        double totalBrut = calculerTotalBrut();
        double remise = 0;
        try {
            remise = discountField.getText().isEmpty() ? 0 : Double.parseDouble(discountField.getText());
        } catch (NumberFormatException e) {
            remise = 0;
        }
        return totalBrut * (1 - remise / 100.0);
    }

    private double calculerMonnaieARendre() {
        double total = calculerTotalApresRemise();
        double recu = 0;
        try {
            recu = amountReceivedField.getText().isEmpty() ? 0 : Double.parseDouble(amountReceivedField.getText());
        } catch (NumberFormatException e) {
            recu = 0;
        }
        return Math.max(0, recu - total);
    }
    
    @FXML
    private void handleValidate() {
        if (cartItems.isEmpty()) {
            showError("Le panier est vide", null);
            return;
        }
        
        try {
            double amountReceived = Double.parseDouble(amountReceivedField.getText());
            if (amountReceived < currentVente.getTotal()) {
                showError("Montant insuffisant", null);
                return;
            }
            
            // Save sale
            currentVente.setLignes(cartItems);
            venteDAO.save(currentVente);
            
            // Print receipt
            printReceipt();
            
            // Reset
            resetCart();
            
        } catch (NumberFormatException e) {
            showError("Montant invalide", null);
        } catch (SQLException e) {
            showError("Erreur lors de l'enregistrement de la vente", e);
        }
    }
    
    @FXML
    private void handleCancel() {
        resetCart();
    }
    
    private void resetCart() {
        cartItems.clear();
        currentVente = new Vente();
        currentVente.setUtilisateurId(currentUser.getId());
        discountField.clear();
        amountReceivedField.clear();
        changeLabel.setText(currencyFormat.format(0));
        updateTotal();
    }
    
    private void printReceipt() {
        try {
            // Get store settings
            Map<String, String> settings = settingsDAO.getAllSettings();
            
            // Create receipt content
            StringBuilder receipt = new StringBuilder();
            receipt.append(settings.get("nom_magasin")).append("\n");
            receipt.append(settings.get("adresse")).append("\n");
            receipt.append(settings.get("telephone")).append("\n");
            receipt.append(settings.get("email")).append("\n\n");
            
            receipt.append("Date: ").append(currentVente.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).append("\n");
            receipt.append("Vendeur: ").append(currentUser.getLogin()).append("\n\n");
            
            receipt.append("Articles:\n");
            for (LigneVente ligne : cartItems) {
                receipt.append(String.format("%s x%d %.2f€ = %.2f€\n",
                    ligne.getProduit().getNom(),
                    ligne.getQuantite(),
                    ligne.getPrixUnitaire(),
                    ligne.getSousTotal()));
            }
            
            receipt.append("\n");
            if (currentVente.getRemise() > 0) {
                receipt.append(String.format("Remise: %.0f%%\n", currentVente.getRemise()));
            }
            receipt.append(String.format("Total: %.2f€\n", currentVente.getTotal()));
            
            receipt.append("\n").append(settings.get("pied_ticket")).append("\n");
            
            // Print receipt
            String printerName = settings.get("imprimante");
            if (printerName != null && !printerName.isEmpty()) {
                Printer printer = Printer.getAllPrinters().stream()
                    .filter(p -> p.getName().equals(printerName))
                    .findFirst()
                    .orElse(Printer.getDefaultPrinter());
                
                if (printer != null) {
                    PrinterJob job = PrinterJob.createPrinterJob(printer);
                    if (job != null) {
                        Text text = new Text(receipt.toString());
                        text.setFont(Font.font("Courier New", 12));
                        
                        // Create a page layout for the receipt
                        PageLayout pageLayout = printer.createPageLayout(
                            Paper.A4,
                            PageOrientation.PORTRAIT,
                            Printer.MarginType.HARDWARE_MINIMUM
                        );
                        
                        job.getJobSettings().setPageLayout(pageLayout);
                        job.printPage(text);
                        job.endJob();
                    }
                }
            }
            
            // Show receipt in dialog
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Facture");
            alert.setHeaderText(null);
            alert.setContentText(receipt.toString());
            alert.showAndWait();
            
        } catch (Exception e) {
            Logger.error("Error printing receipt", e);
            showError("Erreur lors de l'impression de la facture", e);
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
} 