package systems.mythical.cloudcore.core;

public class CloudCoreLogic {
    /**
     * Generates a secure string token for the user.
     * 
     * @param username The username of the user.
     * @param uuid The UUID of the user.
     * 
     * @return The secure string token.
     */
    public static String generateSecureStringToken(String username, String uuid) {
        try {
            // Get current timestamp
            String timestamp = java.time.LocalDateTime.now().toString();
            
            // Generate random bytes
            byte[] randomBytes = new byte[16];
            new java.security.SecureRandom().nextBytes(randomBytes);
            
            // Combine components and encrypt
            String tokenData = timestamp + uuid + 
                             java.util.Base64.getEncoder().encodeToString(randomBytes) + 
                             java.util.Base64.getEncoder().encodeToString(username.getBytes());
            
            // Use SHA-256 for secure hashing
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(tokenData.getBytes());
            
            return "cloud_" + java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate secure token", e);
        }
    }

    public static int generateRandomNumber(int min, int max) {
        return new java.security.SecureRandom().nextInt(max - min + 1) + min;
    }
}
