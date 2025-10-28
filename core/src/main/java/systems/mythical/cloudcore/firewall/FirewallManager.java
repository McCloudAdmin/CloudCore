package systems.mythical.cloudcore.firewall;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.users.IPRelationshipManager;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.settings.Setting;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FirewallManager {
    private static FirewallManager instance;
    @SuppressWarnings("unused")
	private final DatabaseManager databaseManager;
    private final ProxyListManager proxyListManager;
    private final IPRelationshipManager ipRelationshipManager;
    private final UserManager userManager;
    private final CloudSettings settings;
    private final MessageManager messageManager;
    private final CloudLogger cloudLogger;

    // Define settings
    private static final Setting<Boolean> FIREWALL_ENABLED = new CommonSettings.BooleanSetting("firewall_enabled", false);
    private static final Setting<Boolean> FIREWALL_BLOCK_VPN = new CommonSettings.BooleanSetting("firewall_block_vpn", false);
    private static final Setting<Boolean> FIREWALL_BLOCK_ALTS = new CommonSettings.BooleanSetting("firewall_block_alts", false);

    private FirewallManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.proxyListManager = ProxyListManager.getInstance(databaseManager, platformLogger);
        this.ipRelationshipManager = IPRelationshipManager.getInstance(databaseManager, platformLogger);
        this.userManager = UserManager.getInstance(databaseManager, platformLogger);
        this.settings = CloudSettings.getInstance(databaseManager, platformLogger);
        this.messageManager = MessageManager.getInstance(databaseManager, platformLogger);
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public static FirewallManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new FirewallManager(databaseManager, logger);
        }
        return instance;
    }

    public boolean isFirewallEnabled() {
        return getSetting(FIREWALL_ENABLED);
    }

    public boolean isBlockVPNEnabled() {
        return getSetting(FIREWALL_BLOCK_VPN);
    }

    public boolean isBlockAltsEnabled() {
        return getSetting(FIREWALL_BLOCK_ALTS);
    }

    private <T> T getSetting(Setting<T> setting) {
        String value = settings.getSetting(setting.getName());
        if (value.isEmpty()) {
            return setting.getDefaultValue();
        }
        try {
            return setting.parseValue(value);
        } catch (Exception e) {
            cloudLogger.warn("Error parsing setting " + setting.getName() + ": " + e.getMessage());
            return setting.getDefaultValue();
        }
    }

    public FirewallCheckResult checkConnection(String username, String ip) {
        if (!isFirewallEnabled()) {
            return new FirewallCheckResult(true, null);
        }

        // Check for VPN/proxy
        if (isBlockVPNEnabled() && proxyListManager.isProxyIP(ip)) {
            String message = messageManager.getColoredMessage("connection_blocked_vpn");
            return new FirewallCheckResult(false, message);
        }

        // Check for alt accounts
        if (isBlockAltsEnabled()) {
            List<String> relatedUsers = ipRelationshipManager.getIPRelationships(ip).stream()
                .map(relationship -> {
                    try {
                        return userManager.getUserByUuid(UUID.fromString(relationship.getUser()))
                            .map(user -> user.getUsername())
                            .orElse(null);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(name -> name != null && !name.equals(username))
                .collect(Collectors.toList());

            if (!relatedUsers.isEmpty()) {
                String message = messageManager.getColoredMessage("connection_blocked_alts", 
                    String.join(", ", relatedUsers));
                return new FirewallCheckResult(false, message);
            }
        }

        return new FirewallCheckResult(true, null);
    }

    public static class FirewallCheckResult {
        private final boolean allowed;
        private final String reason;

        public FirewallCheckResult(boolean allowed, String reason) {
            this.allowed = allowed;
            this.reason = reason;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public String getReason() {
            return reason;
        }
    }
}
