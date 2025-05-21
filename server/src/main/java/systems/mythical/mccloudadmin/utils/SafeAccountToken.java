package systems.mythical.mccloudadmin.utils;

import java.security.SecureRandom;

public class SafeAccountToken {
    private static final int TOKEN_LENGTH = 32;
    private static final String ALLOWED_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()-_=+";

    /**
     * Generates a cryptographically secure random token for a user account
     * @param username The username to incorporate into the token generation
     * @return A unique, secure token string
     */
    public static String generateToken(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }

        // Create a secure random generator
        SecureRandom secureRandom = new SecureRandom();
        
        // Add current timestamp and username as additional entropy
        String baseString = System.currentTimeMillis() + username;
        secureRandom.setSeed(baseString.getBytes());

        // Generate random token
        StringBuilder token = new StringBuilder();
        for (int i = 0; i < TOKEN_LENGTH; i++) {
            int randomIndex = secureRandom.nextInt(ALLOWED_CHARS.length());
            token.append(ALLOWED_CHARS.charAt(randomIndex));
        }

        // Add a hash of the username as a prefix
        String usernameHash = Integer.toHexString(username.hashCode());
        
        return usernameHash + "_" + token.toString();
    }

    /**
     * Validates if a token matches the expected format and length
     * @param token The token to validate
     * @return true if the token is valid, false otherwise
     */
    public static boolean isValidToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        String[] parts = token.split("_");
        if (parts.length != 2) {
            return false;
        }

        // Verify the random portion length
        return parts[1].length() == TOKEN_LENGTH;
    }
    
}
