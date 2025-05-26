package systems.mythical.cloudcore.permissions;

import java.util.UUID;
import java.util.function.BiFunction;
import java.util.logging.Logger;

public class PlatformPermissionBridge {
    private static final Logger logger = Logger.getLogger(PlatformPermissionBridge.class.getName());
    private static BiFunction<UUID, String, Boolean> permissionChecker;

    private PlatformPermissionBridge() {}

    /**
     * Sets the platform-specific permission checker
     * This should be called by the platform implementation during initialization
     *
     * @param checker The platform-specific permission checking function
     */
    public static void setPermissionChecker(BiFunction<UUID, String, Boolean> checker) {
        permissionChecker = checker;
    }

    /**
     * Checks a permission using the platform-specific checker
     *
     * @param uuid The UUID of the player to check
     * @param permission The permission node to check
     * @return true if the player has the permission, false otherwise
     */
    public static boolean checkPermission(UUID uuid, String permission) {
        if (permissionChecker == null) {
            logger.warning("No permission checker set! Defaulting to false for permission: " + permission);
            return false;
        }
        return permissionChecker.apply(uuid, permission);
    }
} 