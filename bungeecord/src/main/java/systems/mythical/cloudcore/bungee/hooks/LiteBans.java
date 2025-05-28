package systems.mythical.cloudcore.bungee.hooks;

import litebans.api.*;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.core.CloudCore;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.io.File;

public class LiteBans {
    private static final Logger logger = Logger.getLogger(LiteBans.class.getName());
    private static final CloudCore cloudCore = new CloudCore(new File("plugins/CloudCore"), logger);
    private static final DatabaseManager databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
    private static final UserManager userManager = UserManager.getInstance(databaseManager, logger);

    public static void registerEvents() {
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
                        // This is an unmute event.
                        break;
                    case "warn":
                        // This is an unwarn event.
                        break;
                }
            }
        });
    }

    private static void handleBan(Entry entry) {
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
            logger.severe("Error handling ban event: " + e.getMessage());
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
            logger.severe("Error handling unban event: " + e.getMessage());
        }
    }
}
