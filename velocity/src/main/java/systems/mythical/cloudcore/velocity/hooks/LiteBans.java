package systems.mythical.cloudcore.velocity.hooks;

import litebans.api.*;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.core.CloudCore;


import java.util.Optional;
import java.util.UUID;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.io.File;

public class LiteBans {
    private static final CloudLogger logger = CloudLoggerFactory.get();
    private static final CloudCore cloudCore = new CloudCore(new File("plugins/CloudCore"), java.util.logging.Logger.getLogger("CloudCore"), false);
    private static final DatabaseManager databaseManager = new DatabaseManager(cloudCore.getConfig(), java.util.logging.Logger.getLogger("CloudCore"));
    private static final UserManager userManager = UserManager.getInstance(databaseManager, java.util.logging.Logger.getLogger("CloudCore"));

    public LiteBans(CloudCoreVelocity cloudCoreVelocity) {
    }

    public void registerEvents() {
        Events.get().register(new Events.Listener() {
            @Override
            public void entryAdded(Entry entry) {
                switch (entry.getType()) {
                    case "ban":
                        // Handle ban event
                        handleBan(entry);
                        break;
                    case "mute":
                        // This is a mute event.
                        break;
                    case "warn":
                        // This is a warn event.
                        break;
                    case "kick":
                        // This is a kick event.
                        break;
                }
            }

            @Override
            public void entryRemoved(Entry entry) {
                switch (entry.getType()) {
                    case "ban":
                        // Handle unban event
                        handleUnban(entry);
                        break;
                    case "mute":
                        break;
                    case "warn":
                        // This is an unwarn event.
                        break;
                }
            }
        });
    }

    private void handleBan(Entry entry) {
        try {
            String uuid = entry.getUuid();
            if (uuid != null) {
                Optional<User> userOpt = userManager.getUserByUuid(UUID.fromString(uuid));
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setBanned(true);
                    user.setBanReason(entry.getReason());
                    userManager.updateUser(user);
                    logger.info("Updated user " + user.getUsername() + " ban status in panel");
                }
            }
        } catch (Exception e) {
            logger.error("Error handling ban event: " + e.getMessage());
        }
    }

    private static void handleUnban(Entry entry) {
        try {
            String uuid = entry.getUuid();
            if (uuid != null) {
                Optional<User> userOpt = userManager.getUserByUuid(UUID.fromString(uuid));
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    user.setBanned(false);
                    user.setBanReason(null);
                    userManager.updateUser(user);
                    logger.info("Updated user " + user.getUsername() + " unban status in panel");
                }
            }
        } catch (Exception e) {
            logger.error("Error handling unban event: " + e.getMessage());
        }
    }
}
