package systems.mythical.cloudcore.permissions;

import java.util.UUID;
import java.util.logging.Logger;

public class PermissionChecker {
    private static PermissionChecker instance;
    private static final Logger logger = Logger.getLogger(PermissionChecker.class.getName());

    private PermissionChecker() {}

    public static PermissionChecker getInstance() {
        if (instance == null) {
            instance = new PermissionChecker();
        }
        return instance;
    }

    /**
     * Checks if a player has a specific permission
     * This method will delegate to the appropriate platform's permission system
     * through the KickExecutorFactory's platform-specific executor
     *
     * @param uuid The UUID of the player to check
     * @param permission The permission node to check
     * @return true if the player has the permission, false otherwise
     */
    public boolean hasPermission(UUID uuid, String permission) {
        try {
            // We'll use the platform's permission system through the executor
            return PlatformPermissionBridge.checkPermission(uuid, permission);
        } catch (Exception e) {
            logger.severe("Error checking permission " + permission + " for UUID " + uuid + ": " + e.getMessage());
            // Default to false if there's an error
            return false;
        }
    }
} 