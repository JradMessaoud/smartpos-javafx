package com.smartpos.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private static final String DB_URL = "jdbc:sqlite:smartpos.db";
    private static Connection connection;
    
    static {
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Create database connection
            connection = DriverManager.getConnection(DB_URL);
            
            // Initialize database schema
            initializeDatabase();
            
            Logger.info("Database connection established");
        } catch (Exception e) {
            Logger.error("Failed to initialize database", e);
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    
    private static void initializeDatabase() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Database.class.getResourceAsStream("/db/init.sql")))) {
            
            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                // Skip comments and empty lines
                if (line.trim().startsWith("--") || line.trim().isEmpty()) {
                    continue;
                }
                sql.append(line);
                
                // Execute when we find a semicolon
                if (line.trim().endsWith(";")) {
                    try (Statement stmt = connection.createStatement()) {
                        String query = sql.toString().trim();
                        if (!query.isEmpty()) {
                            Logger.info("Executing SQL: " + query);
                            stmt.execute(query);
                        }
                    } catch (SQLException e) {
                        Logger.error("Error executing SQL: " + sql.toString(), e);
                        throw e;
                    }
                    sql.setLength(0);
                }
            }
            
            Logger.info("Database schema initialized successfully");
        } catch (Exception e) {
            Logger.error("Failed to initialize database schema", e);
            throw new RuntimeException("Failed to initialize database schema", e);
        }
    }
    
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
            return connection;
        } catch (SQLException e) {
            Logger.error("Failed to get database connection", e);
            throw new RuntimeException("Failed to get database connection", e);
        }
    }
    
    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Logger.info("Database connection closed");
            }
        } catch (SQLException e) {
            Logger.error("Failed to close database connection", e);
        }
    }
} 