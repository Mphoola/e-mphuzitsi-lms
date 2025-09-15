package com.mphoola.e_empuzitsi.util;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Utility class for generating secure passwords
 */
@Component
public class PasswordGenerator {
    
    private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARS = "!@#$%^&*";
    
    private static final String ALL_CHARS = UPPER_CASE + LOWER_CASE + DIGITS + SPECIAL_CHARS;
    private static final SecureRandom random = new SecureRandom();
    
    /**
     * Generate a secure random password
     * 
     * @param length Password length (minimum 8)
     * @return Generated password
     */
    public String generatePassword(int length) {
        if (length < 8) {
            length = 8; // Minimum password length
        }
        
        StringBuilder password = new StringBuilder();
        
        // Ensure password contains at least one character from each category
        password.append(UPPER_CASE.charAt(random.nextInt(UPPER_CASE.length())));
        password.append(LOWER_CASE.charAt(random.nextInt(LOWER_CASE.length())));
        password.append(DIGITS.charAt(random.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARS.charAt(random.nextInt(SPECIAL_CHARS.length())));
        
        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            password.append(ALL_CHARS.charAt(random.nextInt(ALL_CHARS.length())));
        }
        
        // Shuffle the password to randomize character positions
        return shuffleString(password.toString());
    }
    
    /**
     * Generate a secure password with default length of 12 characters
     * 
     * @return Generated password
     */
    public String generatePassword() {
        return generatePassword(12);
    }
    
    /**
     * Shuffle the characters in a string
     */
    private String shuffleString(String string) {
        char[] chars = string.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }
}