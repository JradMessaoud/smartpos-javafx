package com.smartpos.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BackupManager {
    private static final String DATABASE_FILE = "smartpos.db";
    private static final String BACKUP_DIR = "backups";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    static {
        try {
            Files.createDirectories(Paths.get(BACKUP_DIR));
        } catch (IOException e) {
            Logger.error("Failed to create backup directory", e);
        }
    }
    
    // Method to create a backup with a specified target file path
    public static void createBackup(String targetFilePath) throws IOException, SQLException {
        // Close the current connection to ensure the database file is not locked
        Database.closeConnection();
        
        Path source = Paths.get(DATABASE_FILE);
        Path target = Paths.get(targetFilePath);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        
        Logger.info("Created backup at: " + targetFilePath);
        
        // Re-establish connection if needed later (though not strictly necessary for backup)
        // Database.getConnection(); 
    }
    
    // Original method to create backup with timestamp (can keep for internal use if needed)
     public static String createTimestampedBackup() throws IOException, SQLException {
        String timestamp = LocalDateTime.now().format(DATE_FORMAT);
        String backupFile = BACKUP_DIR + File.separator + "backup_" + timestamp + ".db";
        createBackup(backupFile); // Use the new method
        return backupFile;
     }
    
    // Method to restore backup from a specified source file path
    public static void restoreDatabase(String sourceFilePath) throws IOException, SQLException {
        // Close the current connection
        Database.closeConnection();
        
        // Copy the backup file to the database file
        Path source = Paths.get(sourceFilePath);
        Path target = Paths.get(DATABASE_FILE);
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        
        Logger.info("Restored database from: " + sourceFilePath);
        
        // Re-establish connection after restoring
        Database.getConnection();
    }
    
    public static String[] listBackups() {
        File backupDir = new File(BACKUP_DIR);
        File[] files = backupDir.listFiles((dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
        
        if (files == null) {
            return new String[0];
        }
        
        String[] backupFiles = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            backupFiles[i] = files[i].getName();
        }
        
        return backupFiles;
    }
    
    public static void deleteBackup(String backupFile) throws IOException {
        Path path = Paths.get(BACKUP_DIR, backupFile);
        Files.delete(path);
        Logger.info("Deleted backup: " + backupFile);
    }
} 