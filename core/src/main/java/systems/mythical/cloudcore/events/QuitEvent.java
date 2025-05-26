package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.users.UserActivityManager;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

public class QuitEvent {
    private static final Logger logger = Logger.getLogger(QuitEvent.class.getName());
    private static UserManager userManager;
    private static UserActivityManager activityManager;

    public static void initialize(DatabaseManager databaseManager) {
        userManager = UserManager.getInstance(databaseManager, logger);
        activityManager = UserActivityManager.getInstance(databaseManager, logger);
    }

    public static void onPlayerQuit(String username, UUID uuid) {
        if (userManager == null || activityManager == null) {
            logger.severe("Managers not initialized! Call QuitEvent.initialize() first.");
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                Optional<User> userOpt = userManager.getUserByUuid(uuid);
                if (userOpt.isPresent()) {

                    User user = userOpt.get();

                    // Log the logout activity
                    String context = String.format("Last server: %s", user.getUserConnectedServerName());
                    activityManager.logActivity(uuid.toString(), "game:quit:server", user.getLastIp(), context);

                    user.setUserOnline(false);
                    user.setUserConnectedServerName("Unknown");
                    user.setUserClientName("Unknown");
                    user.setUserVersion("Unknown");
                    user.setLastSeen(LocalDateTime.now());
                    userManager.updateUser(user);

                    logger.info("Updated user status for: " + username + " (" + uuid + ")");
                } else {
                    logger.warning("User not found for quit event: " + username + " (" + uuid + ")");
                }
            } catch (Exception e) {
                logger.severe("Error handling player quit: " + e.getMessage());
            }
        });
    }
}