package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class JoinEvent {
    private static final Logger logger = Logger.getLogger(JoinEvent.class.getName());
    private static UserManager userManager;

    public static void initialize(DatabaseManager databaseManager) {
        userManager = UserManager.getInstance(databaseManager, logger);
    }

    public static void onPlayerJoin(String username, UUID uuid, String ip, String userVersion, String clientName,
            String serverName) {
        if (userManager == null) {
            logger.severe("UserManager not initialized! Call JoinEvent.initialize() first.");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                // First, try to find the user by username
                Optional<User> existingUser = userManager.getUserByUsername(username);

                if (existingUser.isPresent()) {
                    // User exists, update their information
                    User user = existingUser.get();

                    // Update UUID if it changed
                    if (!user.getUuid().equals(uuid)) {
                        user.setUuid(uuid);
                    }

                    // Update last IP and last seen
                    user.setLastIp(ip);
                    user.setLastSeen(LocalDateTime.now());

                    // Update client information
                    user.setUserVersion(userVersion);
                    user.setUserClientName(clientName);
                    user.setUserConnectedServerName(serverName);

                    // Save the changes
                    userManager.updateUser(user);
                    logger.info("Updated existing user: " + username + " (" + uuid + ")");
                } else {
                    // User doesn't exist, create new user
                    User newUser = userManager.createUser(username, uuid, ip);
                    if (newUser != null) {
                        // Set client information for new user
                        newUser.setUserVersion(userVersion);
                        newUser.setUserClientName(clientName);
                        newUser.setUserConnectedServerName(serverName);
                        userManager.updateUser(newUser);
                        logger.info("Created new user: " + username + " (" + uuid + ")");
                    } else {
                        logger.severe("Failed to create new user: " + username + " (" + uuid + ")");
                    }
                }
            } catch (Exception e) {
                logger.severe("Error handling player join: " + e.getMessage());
            }
        });
    }
}
