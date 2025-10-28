package systems.mythical.cloudcore.console;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

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
    private final CloudLogger cloudLogger;

    private ConsoleTaskManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public static ConsoleTaskManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new ConsoleTaskManager(databaseManager, logger);
        }
        return instance;
    }

    public List<ConsoleTask> getPendingTasks() {
        List<ConsoleTask> tasks = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String sql = """
                SELECT * FROM mccloudadmin_console_tasks 
                WHERE executed = 'false' 
                ORDER BY created_at ASC
            """;
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        tasks.add(new ConsoleTask(
                            rs.getInt("id"),
                            rs.getString("cmd"),
                            rs.getString("execute_on_server"),
                            rs.getTimestamp("updated_at").getTime()
                        ));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to get pending console tasks: " + e.getMessage());
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
            cloudLogger.error("Failed to mark console task as executed: " + e.getMessage());
        }
    }
} 