package systems.mythical.cloudcore.bungee;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

public class WorkerManager {
    private final CloudCoreConfig config;
    private final CloudLogger cloudLogger;
    private final DatabaseManager databaseManager;

    public WorkerManager(CloudCoreConfig config, DatabaseManager databaseManager, Logger logger) {
        this.config = config;
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public void initialize() {
        if (validateWorkerCredentials()) {
            if (!workerExists()) {
                registerWorker();
            } else {
                cloudLogger.debug("Worker already registered in database");
            }
            cloudLogger.info("Worker initialized successfully!");
        } else {
            cloudLogger.error("Failed to initialize worker due to invalid credentials");
        }
    }

    private boolean validateWorkerCredentials() {
        String name = config.getWorkerName();
        String key = config.getWorkerKey();
        String uuid = config.getWorkerUUID();

        if (name == null || name.trim().isEmpty()) {
            cloudLogger.error("Worker name is empty in config");
            return false;
        }
        if (key == null || key.trim().isEmpty()) {
            cloudLogger.error("Worker key is empty in config");
            return false;
        }
        if (uuid == null || uuid.trim().isEmpty()) {
            cloudLogger.error("Worker UUID is empty in config");
            return false;
        }

        return true;
    }

    private boolean workerExists() {
        String checkSQL = "SELECT COUNT(*) FROM mccloudadmin_workers WHERE uuid = ?";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(checkSQL)) {
            
            statement.setString(1, config.getWorkerUUID());
            
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Failed to check if worker exists: " + e.getMessage());
        }
        return false;
    }

    private void registerWorker() {
        String insertSQL = "INSERT INTO mccloudadmin_workers (name, secret, uuid) VALUES (?, ?, ?)";
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement statement = connection.prepareStatement(insertSQL)) {
            
            statement.setString(1, config.getWorkerName().trim());
            statement.setString(2, config.getWorkerKey().trim());
            statement.setString(3, config.getWorkerUUID().trim());
            
            statement.executeUpdate();
            cloudLogger.info("Worker registered successfully in database");
        } catch (SQLException e) {
            cloudLogger.error("Failed to register worker: " + e.getMessage());
        }
    }
}
