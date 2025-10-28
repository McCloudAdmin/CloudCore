package systems.mythical.cloudcore.chatlog;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class ChatLogManager {
    private static ChatLogManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;
    private final Gson gson;

    private ChatLogManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
        this.gson = new Gson();
    }

    public static ChatLogManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ChatLogManager(databaseManager, logger);
        }
        return instance;
    }

    public ChatLog createChatLog(User user, User sender) {
        try (Connection conn = databaseManager.getConnection()) {
            // First, get the last 45 messages for the user
            List<Map<String, Object>> messages = getLastMessages(user.getUuid(), 45);
            
            // Insert the chat log request
            String sql = "INSERT INTO mccloudadmin_chatlogs_requests (uuid, messages, uuid_reported) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, user.getUuid().toString());
                stmt.setString(3, sender.getUuid().toString());
                stmt.setString(2, gson.toJson(messages));
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int id = rs.getInt(1);
                        return new ChatLog(
                            id,
                            user.getUuid(),
                            messages,
                            System.currentTimeMillis(),
                            System.currentTimeMillis(),
                            false,
                            false
                        );
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to create chat log: " + e.getMessage());
        }
        return null;
    }

    private List<Map<String, Object>> getLastMessages(UUID userId, int limit) {
        List<Map<String, Object>> messages = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT * FROM mccloudadmin_chatlogs 
                WHERE uuid = ? AND deleted = 'false'
                ORDER BY created_at DESC 
                LIMIT ?
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId.toString());
                stmt.setInt(2, limit);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        Map<String, Object> message = Map.of(
                            "id", rs.getInt("id"),
                            "content", rs.getString("content"),
                            "server", rs.getString("server"),
                            "created_at", rs.getTimestamp("created_at").getTime()
                        );
                        messages.add(message);
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to get last messages: " + e.getMessage());
        }
        return messages;
    }

    public Optional<ChatLog> getChatLog(int id) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT * FROM mccloudadmin_chatlogs_requests WHERE id = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        List<Map<String, Object>> messages = gson.fromJson(
                            rs.getString("messages"),
                            new TypeToken<List<Map<String, Object>>>(){}.getType()
                        );
                        return Optional.of(new ChatLog(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            messages,
                            rs.getTimestamp("created_at").getTime(),
                            rs.getTimestamp("updated_at").getTime(),
                            rs.getString("locked").equals("true"),
                            rs.getString("deleted").equals("true")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to get chat log: " + e.getMessage());
        }
        return Optional.empty();
    }

    public List<ChatLog> getChatLogsByUser(UUID userId) {
        List<ChatLog> chatLogs = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "SELECT * FROM mccloudadmin_chatlogs_requests WHERE uuid = ? AND deleted = 'false' ORDER BY created_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, userId.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        List<Map<String, Object>> messages = gson.fromJson(
                            rs.getString("messages"),
                            new TypeToken<List<Map<String, Object>>>(){}.getType()
                        );
                        chatLogs.add(new ChatLog(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            messages,
                            rs.getTimestamp("created_at").getTime(),
                            rs.getTimestamp("updated_at").getTime(),
                            rs.getString("locked").equals("true"),
                            rs.getString("deleted").equals("true")
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to get chat logs for user: " + e.getMessage());
        }
        return chatLogs;
    }
}
