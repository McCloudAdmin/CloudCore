package systems.mythical.cloudcore.commands;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class CommandLogManager {
    private static CommandLogManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    private CommandLogManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static CommandLogManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new CommandLogManager(databaseManager, logger);
        }
        return instance;
    }

    /**
     * Logs a command to the database
     *
     * @param uuid The UUID of the player who executed the command
     * @param content The full command string (including the /)
     * @param server The server where the command was executed
     */
    public void logCommand(UUID uuid, String content, String server) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_cmdlogs (uuid, content, server) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, content);
                stmt.setString(3, server);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.severe("Error logging command for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Logs a command to the database asynchronously
     *
     * @param uuid The UUID of the player who executed the command
     * @param content The full command string (including the /)
     * @param server The server where the command was executed
     */
    public void logCommandAsync(UUID uuid, String content, String server) {
        new Thread(() -> logCommand(uuid, content, server)).start();
    }
} 