package systems.mythical.cloudcore.console;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class ConsoleTaskManager {
    private static ConsoleTaskManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;
    private final String platform;

    private ConsoleTaskManager(DatabaseManager databaseManager, Logger logger, String platform) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.platform = platform;
        createTable();
    }

    public static ConsoleTaskManager getInstance(DatabaseManager databaseManager, Logger logger, String platform) {
        if (instance == null) {
            instance = new ConsoleTaskManager(databaseManager, logger, platform);
        }
        return instance;
    }

    private void createTable() {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS mccloudadmin_console_tasks (
                    id INT NOT NULL AUTO_INCREMENT,
                    cmd TEXT NOT NULL,
                    executed ENUM('true', 'false') NOT NULL DEFAULT 'false',
                    execute_on ENUM('proxy', 'server') NOT NULL DEFAULT 'server',
                    execute_on_server TEXT NOT NULL,
                    date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    PRIMARY KEY (id)
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.severe("Failed to create console_tasks table: " + e.getMessage());
        }
    }

    public List<ConsoleTask> getPendingTasks() {
        List<ConsoleTask> tasks = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT * FROM mccloudadmin_console_tasks 
                WHERE executed = 'false' 
                AND execute_on = ? 
                ORDER BY date ASC
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, platform);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tasks.add(new ConsoleTask(
                            rs.getInt("id"),
                            rs.getString("cmd"),
                            rs.getString("execute_on"),
                            rs.getString("execute_on_server"),
                            rs.getTimestamp("date").getTime()
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to get pending console tasks: " + e.getMessage());
        }
        return tasks;
    }

    public void markTaskAsExecuted(int taskId) {
        try (Connection conn = databaseManager.getConnection()) {
            String sql = "UPDATE mccloudadmin_console_tasks SET executed = 'true' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, taskId);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.severe("Failed to mark console task as executed: " + e.getMessage());
        }
    }
} 