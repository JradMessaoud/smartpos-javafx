package com.smartpos.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "smartpos.log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static Path logPath;
    
    static {
        try {
            // Create logs directory if it doesn't exist
            Files.createDirectories(Paths.get(LOG_DIR));
            logPath = Paths.get(LOG_DIR, LOG_FILE);
            
            // Create log file if it doesn't exist
            if (!Files.exists(logPath)) {
                Files.createFile(logPath);
            }
        } catch (IOException e) {
            System.err.println("Failed to initialize logger: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void log(String level, String message, Throwable throwable) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String timestamp = now.format(DATE_FORMAT);
            String threadName = Thread.currentThread().getName();
            
            StringBuilder logEntry = new StringBuilder()
                .append(timestamp)
                .append(" [").append(level).append("] ")
                .append("[").append(threadName).append("] ")
                .append(message);
            
            if (throwable != null) {
                logEntry.append("\n").append(throwable.toString());
                for (StackTraceElement element : throwable.getStackTrace()) {
                    logEntry.append("\n\tat ").append(element);
                }
            }
            
            logEntry.append("\n");
            
            // Write to file
            Files.write(logPath, logEntry.toString().getBytes(), StandardOpenOption.APPEND);
            
            // Also print to console
            System.out.println(logEntry.toString());
            
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void info(String message) {
        log("INFO", message, null);
    }
    
    public static void info(String message, Throwable throwable) {
        log("INFO", message, throwable);
    }
    
    public static void error(String message) {
        log("ERROR", message, null);
    }
    
    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }
    
    public static void warn(String message) {
        log("WARN", message, null);
    }
    
    public static void warn(String message, Throwable throwable) {
        log("WARN", message, throwable);
    }
    
    public static void debug(String message) {
        log("DEBUG", message, null);
    }
    
    public static void debug(String message, Throwable throwable) {
        log("DEBUG", message, throwable);
    }
    
    public static void clearLogs() {
        try {
            Files.deleteIfExists(logPath);
            Files.createFile(logPath);
            info("Log file cleared");
        } catch (IOException e) {
            System.err.println("Failed to clear log file: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 