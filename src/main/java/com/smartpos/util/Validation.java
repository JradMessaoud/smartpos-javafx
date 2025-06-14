package com.smartpos.util;

import java.util.regex.Pattern;

public class Validation {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$",
        Pattern.CASE_INSENSITIVE
    );
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^\\+?[0-9]{10,15}$"
    );
    
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }
    
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    public static boolean isValidLogin(String login) {
        return login != null && login.length() >= 3 && login.matches("^[a-zA-Z0-9_]+$");
    }
    
    public static boolean isValidPrice(double price) {
        return price >= 0;
    }
    
    public static boolean isValidQuantity(int quantity) {
        return quantity >= 0;
    }
    
    public static boolean isValidBarcode(String barcode) {
        return barcode == null || barcode.isEmpty() || barcode.matches("^[0-9]{8,13}$");
    }
    
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim()
            .replaceAll("[<>\"']", "") // Remove potential HTML/script tags
            .replaceAll("\\s+", " ");  // Normalize whitespace
    }
} 