package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.users.UserActivityManager;
import systems.mythical.cloudcore.users.IPRelationshipManager;
import systems.mythical.cloudcore.firewall.FirewallManager;
import systems.mythical.cloudcore.kick.KickExecutorFactory;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.PermissionChecker;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class JoinEvent {
    private static final Logger logger = Logger.getLogger(JoinEvent.class.getName());
    private static CloudLogger cloudLogger = CloudLoggerFactory.get();
    private static UserManager userManager;
    private static UserActivityManager activityManager;
    private static IPRelationshipManager ipManager;
    private static FirewallManager firewallManager;
    private static MessageManager messageManager;
    private static PermissionChecker permissionChecker;

    // Permission nodes
    public static final String VPN_BYPASS_PERMISSION = "cloudcore.vpn.bypass";
    public static final String ALTS_BYPASS_PERMISSION = "cloudcore.alts.bypass";

    public static void initialize(DatabaseManager databaseManager) {
        userManager = UserManager.getInstance(databaseManager, logger);
        activityManager = UserActivityManager.getInstance(databaseManager, logger);
        ipManager = IPRelationshipManager.getInstance(databaseManager, logger);
        firewallManager = FirewallManager.getInstance(databaseManager, logger);
        messageManager = MessageManager.getInstance(databaseManager, logger);
        permissionChecker = PermissionChecker.getInstance();
        cloudLogger = CloudLoggerFactory.get();
    }

    public static void onPlayerJoin(String username, UUID uuid, String ip, String userVersion, String clientName,
            String serverName, String group) {
        if (userManager == null || activityManager == null || ipManager == null ||
                firewallManager == null || messageManager == null || permissionChecker == null) {
            cloudLogger.error("Managers not initialized! Call JoinEvent.initialize() first.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                // Check firewall rules (VPN check)
                if (!permissionChecker.hasPermission(uuid, VPN_BYPASS_PERMISSION)) {
                    FirewallManager.FirewallCheckResult checkResult = firewallManager.checkConnection(username, ip);
                    if (!checkResult.isAllowed()) {
                        cloudLogger.warn(
                                "Connection blocked for " + username + " (" + uuid + "): " + checkResult.getReason());
                        // Execute kick directly through platform executor
                        KickExecutorFactory.getExecutor().executeKick(uuid, checkResult.getReason());
                        return;
                    }
                } else {
                    cloudLogger.debug("Player " + username + " bypassed VPN check with permission: " + VPN_BYPASS_PERMISSION);
                }

                // First, try to find the user by username
                Optional<User> existingUser = userManager.getUserByUsername(username);

                if (existingUser.isPresent()) {
                    // User exists, update their information
                    User user = existingUser.get();

                    // Check for alt accounts only if they don't have bypass permission
                    if (!permissionChecker.hasPermission(uuid, ALTS_BYPASS_PERMISSION)) {
                        if (user.getUuid() != null && !user.getUuid().equals(uuid)) {
                            String reason = "This username is already registered with a different UUID. If this is your account, please contact staff.";
                            KickExecutorFactory.getExecutor().executeKick(uuid, reason);
                            cloudLogger.warn("Alt account detected for username " + username + ": Old UUID = " + user.getUuid() + ", New UUID = " + uuid);
                            return;
                        }
                    } else {
                        cloudLogger.debug("Player " + username + " bypassed alt account check with permission: " + ALTS_BYPASS_PERMISSION);
                    }

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
                    user.setUserOnline(true);
                    user.setUserGroup(group);

                    // Save the changes
                    userManager.updateUser(user);

                    // Log the login activity
                    String context = String.format("Client: %s, Version: %s, Server: %s", clientName, userVersion,
                            serverName);
                    activityManager.logActivity(uuid.toString(), "game:join:server", ip, context);

                    // Track IP relationship
                    ipManager.addIPRelationship(uuid.toString(), ip);

                    cloudLogger.debug("Updated existing user: " + username + " (" + uuid + ")");
                } else {
                    // User doesn't exist, create new user
                    User newUser = userManager.createUser(username, uuid, ip);
                    if (newUser != null) {
                        // Set client information for new user
                        newUser.setUserVersion(userVersion);
                        newUser.setUserClientName(clientName);
                        newUser.setUserConnectedServerName(serverName);
                        newUser.setUserOnline(true);
                        newUser.setUserGroup(group);
                        userManager.updateUser(newUser);

                        // Log the first login activity
                        String context = String.format("First login - Client: %s, Version: %s, Server: %s", clientName,
                                userVersion, serverName);
                        activityManager.logActivity(uuid.toString(), "game:join:server", ip, context);

                        // Track IP relationship for new user
                        ipManager.addIPRelationship(uuid.toString(), ip);

                        cloudLogger.info("Created new user: " + username + " (" + uuid + ")");
                    } else {
                        cloudLogger.error("Failed to create new user: " + username + " (" + uuid + ")");
                    }
                }
            } catch (Exception e) {
                cloudLogger.error("Error handling player join: " + e.getMessage());
            }
        });
    }
}
