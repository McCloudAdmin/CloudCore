package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.chat.ChatLogManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.PermissionChecker;
import systems.mythical.cloudcore.redis.RedisManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;

import java.util.UUID;
import java.util.logging.Logger;

public class ChatEvent {
    private static final Logger logger = Logger.getLogger(ChatEvent.class.getName());
    private static ChatLogManager chatLogManager;
    private static PermissionChecker permissionChecker;
    private static CloudSettings cloudSettings;
    private static RedisManager redisManager;

    // Permission nodes
    public static final String CHAT_BYPASS_PERMISSION = "cloudcore.chat.bypass";

    // Settings
    private static final CommonSettings.BooleanSetting LOG_CHAT_LOGS = new CommonSettings.BooleanSetting("log_chatlogs", true);

    public static void initialize(DatabaseManager databaseManager) {
        chatLogManager = ChatLogManager.getInstance(databaseManager, logger);
        permissionChecker = PermissionChecker.getInstance();
        cloudSettings = CloudSettings.getInstance(databaseManager, logger);
        
        // Initialize Redis manager if Redis settings are available
        try {
            String redisHost = cloudSettings.getSetting("redis_host");
            String redisPort = cloudSettings.getSetting("redis_port");
            String redisPassword = cloudSettings.getSetting("redis_password");
            
            if (redisHost != null && redisPort != null) {
                int port = Integer.parseInt(redisPort);
                redisManager = RedisManager.getInstance(redisHost, port, redisPassword, logger);
                logger.info("Redis manager initialized for chat logging");
            } else {
                logger.warning("Redis settings not found, falling back to direct database logging");
            }
        } catch (Exception e) {
            logger.warning("Failed to initialize Redis manager: " + e.getMessage() + ". Falling back to direct database logging.");
        }
    }

    /**
     * Handles a chat message event
     *
     * @param uuid The UUID of the player who sent the message
     * @param content The content of the message
     * @param server The server where the message was sent
     * @return true if the message should be allowed, false if it should be blocked
     */
    public static boolean onPlayerChat(UUID uuid, String content, String server) {
        if (chatLogManager == null || permissionChecker == null || cloudSettings == null) {
            logger.severe("Managers not initialized! Call ChatEvent.initialize() first.");
            return true; // Allow the message if not initialized
        }

        try {
            // Check if chat logging is enabled
            if (!LOG_CHAT_LOGS.parseValue(cloudSettings.getSetting(LOG_CHAT_LOGS.getName()))) {
                return true; // Allow message but don't log it
            }

            // Try to use Redis first, fallback to direct database logging
            if (redisManager != null && redisManager.isAvailable()) {
                // Queue the message in Redis for processing by the worker
                redisManager.queueChatMessage(uuid, content, server, System.currentTimeMillis());
                logger.fine("Chat message queued in Redis for player " + uuid);
            } else {
                // Fallback to direct database logging
                chatLogManager.logChatMessageAsync(uuid, content, server);
                logger.fine("Chat message logged directly to database for player " + uuid);
            }
                        
            return true;
        } catch (Exception e) {
            logger.severe("Error handling chat message: " + e.getMessage());
            return true; // Allow the message if there's an error
        }
    }
} 