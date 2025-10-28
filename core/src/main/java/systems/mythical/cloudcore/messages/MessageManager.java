package systems.mythical.cloudcore.messages;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class MessageManager {
    private static MessageManager instance;
    private final Map<String, String> messageCache;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger = CloudLoggerFactory.get();
    private final ScheduledExecutorService scheduler;

    // Color code patterns
    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-or])");
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9a-fA-F]{6})");

    private MessageManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.messageCache = new HashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Initial load
        refreshMessages();
        
        // Schedule refresh every 15 minutes
        scheduler.scheduleAtFixedRate(this::refreshMessages, 15, 15, TimeUnit.MINUTES);
    }

    public static MessageManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new MessageManager(databaseManager, logger);
        }
        return instance;
    }

    public void refreshMessages() {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT name, value FROM mccloudadmin_messages WHERE deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                Map<String, String> newCache = new HashMap<>();
                while (rs.next()) {
                    String name = rs.getString("name");
                    String value = rs.getString("value");
                    if (name != null && value != null) {
                        newCache.put(name, value);
                    }
                }
                
                synchronized (messageCache) {
                    messageCache.clear();
                    messageCache.putAll(newCache);
                }
                
                cloudLogger.debug("Message cache refreshed successfully");
            }
        } catch (SQLException e) {
            cloudLogger.error("Error refreshing message cache: " + e.getMessage());
        }
    }

    public String getMessage(String name) {
        synchronized (messageCache) {
            return messageCache.getOrDefault(name, "");
        }
    }

    public String getMessage(String name, Object... args) {
        String message = getMessage(name);
        if (message.isEmpty()) {
            return "";
        }
        return String.format(message, args);
    }

    public String getColoredMessage(String name) {
        return parseColorCodes(getMessage(name));
    }

    public String getColoredMessage(String name, Object... args) {
        return parseColorCodes(getMessage(name, args));
    }

    public void setMessage(String name, String value) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_messages (name, value) VALUES (?, ?) " +
                          "ON DUPLICATE KEY UPDATE value = ?, updated_at = CURRENT_TIMESTAMP";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.setString(2, value);
                stmt.setString(3, value);
                stmt.executeUpdate();
                
                synchronized (messageCache) {
                    messageCache.put(name, value);
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error setting message " + name + ": " + e.getMessage());
        }
    }

    public void deleteMessage(String name) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_messages SET deleted = 'true' WHERE name = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, name);
                stmt.executeUpdate();
                
                synchronized (messageCache) {
                    messageCache.remove(name);
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error deleting message " + name + ": " + e.getMessage());
        }
    }

    private String parseColorCodes(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        // Convert all types of newlines to actual newlines
        message = message.replace("\\\\n", "\n")  // Double escaped
                        .replace("\\n", "\n")      // Single escaped
                        .replace("/n", "\n")       // Forward slash
                        .replace("{NL}", "\n")     // Custom token
                        .replace("{nl}", "\n");    // Custom token lowercase

        // Replace color codes
        String colored = COLOR_PATTERN.matcher(message).replaceAll("§$1");
        
        // Replace hex colors
        colored = HEX_PATTERN.matcher(colored).replaceAll("§x§$1§$2§$3§$4§$5§$6");

        // Ensure newlines are preserved in the final message
        colored = colored.replace("\n", "\n§r");  // Reset formatting after each newline
        
        return colored;
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
} 