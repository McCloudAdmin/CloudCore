package systems.mythical.cloudcore.bungee.events;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.event.EventBus;
import net.luckperms.api.event.user.UserDataRecalculateEvent;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.permissions.WebPanelPermissionManager;

import java.util.ArrayList;
import java.util.List;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;
import java.util.UUID;

public class LuckPermsListener {
    @SuppressWarnings("unused")
    private final DatabaseManager databaseManager;
    private final CloudLogger logger;
    private final UserManager userManager;
    private final LuckPerms luckPerms;
    private final WebPanelPermissionManager webPanelPermissionManager;

    public LuckPermsListener(LuckPerms luckPerms, DatabaseManager databaseManager, java.util.logging.Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = CloudLoggerFactory.get();
        this.userManager = UserManager.getInstance(databaseManager, logger);
        this.luckPerms = luckPerms;
        this.webPanelPermissionManager = WebPanelPermissionManager.getInstance(databaseManager, logger);

        // Subscribe to user data recalculation events
        EventBus eventBus = luckPerms.getEventBus();
        eventBus.subscribe(UserDataRecalculateEvent.class, this::handleUserUpdate);
    }

    private void handleUserUpdate(UserDataRecalculateEvent event) {
        User user = event.getUser();
        
        // Update the user's group in the database based on their primary group
        String groupName = user.getPrimaryGroup();
        Group group = luckPerms.getGroupManager().getGroup(groupName);
        int weight = group.getWeight().orElse(0);
        if (group != null) {
            String displayName = group.getDisplayName() != null ? group.getDisplayName() : groupName;
            var cloudUser = userManager.getUserByUuid(user.getUniqueId());
            if (cloudUser.isPresent()) {
                var updatedUser = cloudUser.get();
                updatedUser.setUserGroup(displayName);
                updatedUser.setUserGroupWeight(weight);
                userManager.updateUser(updatedUser);
                logger.debug("Updated group for user " + user.getUsername() + " to " + displayName);
            }
        }

        // Update web panel permissions
        updateWebPanelPermissions(user);
    }

    private void updateWebPanelPermissions(User user) {
        try {
            UUID uuid = user.getUniqueId();
            List<String> webPanelPermissions = new ArrayList<>();
            List<String> negativePermissions = new ArrayList<>();

            // Get all permissions the user has (including inherited ones)
            user.getCachedData().getPermissionData().getPermissionMap().forEach((permission, value) -> {
                if (permission.startsWith("cloudcore.webpanel.")) {
                    if (value) {
                        webPanelPermissions.add(permission);
                    } else {
                        negativePermissions.add(permission);
                    }
                }
            });

            // Update the web panel permissions in the database
            webPanelPermissionManager.updateUserPermissions(uuid, webPanelPermissions, negativePermissions);
            logger.debug("Updated web panel permissions for user " + user.getUsername());
        } catch (Exception e) {
            logger.error("Error updating web panel permissions: " + e.getMessage());
            e.printStackTrace();
        }
    }
} 