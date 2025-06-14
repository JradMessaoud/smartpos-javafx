package com.smartpos.controller;

import com.smartpos.dao.ProduitDAO;
import com.smartpos.dao.VenteDAO;
import com.smartpos.model.Produit;
import com.smartpos.model.Utilisateur;
import com.smartpos.model.Vente;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class StatsController implements MainController.Initializable {
    
    @FXML private DatePicker startDate;
    @FXML private DatePicker endDate;
    
    @FXML private Label totalSalesLabel;
    @FXML private Label salesCountLabel;
    @FXML private Label averageCartLabel;
    
    @FXML private LineChart<String, Number> salesChart;
    @FXML private BarChart<String, Number> topProductsChart;
    @FXML private PieChart categoriesChart;
    
    @FXML private TableView<Produit> lowStockTable;
    @FXML private TableColumn<Produit, String> productNameColumn;
    @FXML private TableColumn<Produit, Integer> stockColumn;
    @FXML private TableColumn<Produit, Integer> alertColumn;
    
    @FXML private TableView<CategoryStats> categoriesTable;
    @FXML private TableColumn<CategoryStats, String> categoryColumn;
    @FXML private TableColumn<CategoryStats, Double> salesColumn;
    @FXML private TableColumn<CategoryStats, Double> percentageColumn;
    
    private VenteDAO venteDAO;
    private ProduitDAO produitDAO;
    private NumberFormat currencyFormat;
    private NumberFormat percentFormat;
    
    public StatsController() {
        this.venteDAO = new VenteDAO();
        this.produitDAO = new ProduitDAO();
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.FRANCE);
        this.percentFormat = NumberFormat.getPercentInstance(Locale.FRANCE);
    }
    
    @Override
    public void initialize(Utilisateur user) {
        setupTableColumns();
        setupDatePickers();
        loadData();
    }
    
    private void setupTableColumns() {
        // Low stock table
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("nom"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("quantite"));
        alertColumn.setCellValueFactory(new PropertyValueFactory<>("seuilAlerte"));
        
        // Categories table
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        salesColumn.setCellValueFactory(new PropertyValueFactory<>("sales"));
        percentageColumn.setCellValueFactory(new PropertyValueFactory<>("percentage"));
    }
    
    private void setupDatePickers() {
        // Set default date range (last 30 days)
        endDate.setValue(LocalDate.now());
        startDate.setValue(LocalDate.now().minusDays(30));
        
        // Add listeners to refresh data when dates change
        startDate.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
        endDate.valueProperty().addListener((obs, oldVal, newVal) -> loadData());
    }
    
    @FXML
    private void handleRefresh() {
        loadData();
    }
    
    @FXML
    private void handleExport() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les statistiques");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Fichiers Excel", "*.xlsx")
        );
        
        File file = fileChooser.showSaveDialog(startDate.getScene().getWindow());
        if (file != null) {
            // TODO: Implement Excel export
            showError("Export non implémenté", null);
        }
    }
    
    private void loadData() {
        try {
            LocalDateTime start = startDate.getValue().atStartOfDay();
            LocalDateTime end = endDate.getValue().atTime(23, 59, 59);
            
            List<Vente> ventes = venteDAO.findByDate(start, end);
            updateSalesOverview(ventes);
            updateSalesChart(ventes);
            updateTopProducts(ventes);
            updateCategories(ventes);
            updateLowStock();
            
        } catch (SQLException e) {
            showError("Erreur lors du chargement des données", e);
        }
    }
    
    private void updateSalesOverview(List<Vente> ventes) {
        double totalSales = ventes.stream()
                .mapToDouble(Vente::getTotal)
                .sum();
        
        int salesCount = ventes.size();
        double averageCart = salesCount > 0 ? totalSales / salesCount : 0;
        
        totalSalesLabel.setText(currencyFormat.format(totalSales));
        salesCountLabel.setText(String.valueOf(salesCount));
        averageCartLabel.setText(currencyFormat.format(averageCart));
    }
    
    private void updateSalesChart(List<Vente> ventes) {
        salesChart.getData().clear();
        
        // Group sales by date
        Map<LocalDate, Double> dailySales = ventes.stream()
                .collect(Collectors.groupingBy(
                    vente -> vente.getDate().toLocalDate(),
                    Collectors.summingDouble(Vente::getTotal)
                ));
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Ventes");
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");
        dailySales.forEach((date, total) -> 
            series.getData().add(new XYChart.Data<>(date.format(formatter), total))
        );
        
        salesChart.getData().add(series);
    }
    
    private void updateTopProducts(List<Vente> ventes) {
        topProductsChart.getData().clear();
        
        // Count product quantities
        Map<String, Integer> productQuantities = new HashMap<>();
        ventes.forEach(vente -> vente.getLignes().forEach(ligne -> {
            Produit produit = ligne.getProduit();
            if (produit != null) {
                String productName = produit.getNom();
                productQuantities.merge(productName, ligne.getQuantite(), Integer::sum);
            }
        }));
        
        // Sort and get top 10
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Quantités vendues");
        
        productQuantities.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> 
                    series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()))
                );
        
        topProductsChart.getData().add(series);
    }
    
    private void updateCategories(List<Vente> ventes) {
        categoriesChart.getData().clear();
        List<CategoryStats> stats = new ArrayList<>();
        
        // Calculate category statistics
        Map<String, Double> categorySales = new HashMap<>();
        double totalSales = ventes.stream()
                .mapToDouble(Vente::getTotal)
                .sum();
        
        ventes.forEach(vente -> vente.getLignes().forEach(ligne -> {
            Produit produit = ligne.getProduit();
            if (produit != null) {
                String category = produit.getCategorie();
                if (category != null && !category.isEmpty()) {
                    categorySales.merge(category, ligne.getSousTotal(), Double::sum);
                }
            }
        }));
        
        // Create pie chart data and table data
        categorySales.forEach((category, sales) -> {
            double percentage = totalSales > 0 ? sales / totalSales : 0;
            categoriesChart.getData().add(new PieChart.Data(category, sales));
            stats.add(new CategoryStats(category, sales, percentage));
        });
        
        categoriesTable.setItems(FXCollections.observableArrayList(stats));
    }
    
    private void updateLowStock() throws SQLException {
        lowStockTable.setItems(FXCollections.observableArrayList(produitDAO.findStockFaible()));
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
    
    // Helper class for category statistics
    public static class CategoryStats {
        private final String category;
        private final double sales;
        private final double percentage;
        
        public CategoryStats(String category, double sales, double percentage) {
            this.category = category;
            this.sales = sales;
            this.percentage = percentage;
        }
        
        public String getCategory() {
            return category;
        }
        
        public double getSales() {
            return sales;
        }
        
        public double getPercentage() {
            return percentage;
        }
    }
} 