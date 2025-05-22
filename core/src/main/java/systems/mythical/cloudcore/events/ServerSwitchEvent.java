package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public class ServerSwitchEvent {
    private static final Logger logger = Logger.getLogger(ServerSwitchEvent.class.getName());
    private static UserManager userManager;

    public static void initialize(DatabaseManager databaseManager) {
        userManager = UserManager.getInstance(databaseManager, logger);
    }

    public static void onServerSwitch(String username, UUID uuid, String serverName) {
        if (userManager == null) {
            logger.severe("UserManager not initialized! Call ServerSwitchEvent.initialize() first.");
            return;
        }

        // Process the server switch asynchronously
        CompletableFuture.runAsync(() -> {
            try {
                Optional<User> user = userManager.getUserByUuid(uuid);
                if (user.isPresent()) {
                    User player = user.get();
                    // Only update if the server has actually changed
                    if (!serverName.equals(player.getUserConnectedServerName())) {
                        player.setUserConnectedServerName(serverName);
                        userManager.updateUser(player);
                        logger.info("Updated server for player: " + username + " to " + serverName);
                    }
                } else {
                    logger.warning("Could not find user for server switch: " + username + " (" + uuid + ")");
                }
            } catch (Exception e) {
                logger.severe("Error handling server switch: " + e.getMessage());
            }
        });
    }
} 