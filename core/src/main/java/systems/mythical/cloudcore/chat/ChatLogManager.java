package systems.mythical.cloudcore.chat;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class ChatLogManager {
    private static ChatLogManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    private ChatLogManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static ChatLogManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ChatLogManager(databaseManager, logger);
        }
        return instance;
    }

    /**
     * Logs a chat message to the database
     *
     * @param uuid The UUID of the player who sent the message
     * @param content The content of the message
     * @param server The server where the message was sent
     */
    public void logChatMessage(UUID uuid, String content, String server) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_chatlogs (uuid, content, server) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, content);
                stmt.setString(3, server);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.severe("Error logging chat message for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Logs a chat message to the database asynchronously
     *
     * @param uuid The UUID of the player who sent the message
     * @param content The content of the message
     * @param server The server where the message was sent
     */
    public void logChatMessageAsync(UUID uuid, String content, String server) {
        new Thread(() -> logChatMessage(uuid, content, server)).start();
    }
} 