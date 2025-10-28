package systems.mythical.cloudcore.console;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;
import java.util.logging.Logger;

public class ConsoleExecutorManager {
    private static ConsoleExecutorManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;

    private ConsoleExecutorManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public static ConsoleExecutorManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ConsoleExecutorManager(databaseManager, logger);
        }
        return instance;
    }

    /**
     * Checks if a player is allowed to execute console commands
     *
     * @param uuid The UUID of the player to check
     * @return true if the player is allowed to execute console commands
     */
    public boolean isConsoleExecutor(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT id FROM mccloudadmin_console_executors WHERE uuid = ? AND locked = 'false' AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Returns true if a record exists
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error checking console executor status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Checks if a player is allowed to execute console commands
     *
     * @param displayName The display name of the player to check
     * @return true if the player is allowed to execute console commands
     */
    public boolean isConsoleExecutor(String displayName) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT ce.id FROM mccloudadmin_console_executors ce " +
                           "JOIN mccloudadmin_users u ON ce.uuid = u.uuid " +
                           "WHERE u.username = ? AND ce.locked = 'false' AND ce.deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, displayName);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next(); // Returns true if a record exists
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error checking console executor status for " + displayName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Adds a player as a console executor
     *
     * @param uuid The UUID of the player to add
     * @return true if the player was successfully added
     */
    public boolean addConsoleExecutor(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_console_executors (uuid) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Added console executor: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error adding console executor " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Removes a player as a console executor
     *
     * @param uuid The UUID of the player to remove
     * @return true if the player was successfully removed
     */
    public boolean removeConsoleExecutor(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_console_executors SET deleted = 'true' WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Removed console executor: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error removing console executor " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Locks a console executor's access
     *
     * @param uuid The UUID of the player to lock
     * @return true if the player was successfully locked
     */
    public boolean lockConsoleExecutor(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_console_executors SET locked = 'true' WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Locked console executor: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error locking console executor " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Unlocks a console executor's access
     *
     * @param uuid The UUID of the player to unlock
     * @return true if the player was successfully unlocked
     */
    public boolean unlockConsoleExecutor(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_console_executors SET locked = 'false' WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Unlocked console executor: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error unlocking console executor " + uuid + ": " + e.getMessage());
            return false;
        }
    }
} 