package systems.mythical.cloudcore.spigot.worker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;
import systems.mythical.cloudcore.config.CloudCoreConfig;
import systems.mythical.cloudcore.database.DatabaseManager;

public class WorkerManager {
    private final CloudCoreConfig config;
    private final Logger logger;
    private final DatabaseManager databaseManager;

    public WorkerManager(CloudCoreConfig config, DatabaseManager databaseManager, Logger logger) {
        this.config = config;
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public void initialize() {
        if (validateWorkerCredentials()) {
            if (!workerExists()) {
                registerWorker();
            } else {
                logger.info("Worker already registered in database");
            }
            logger.info("Worker initialized successfully!");
        } else {
            logger.severe("Failed to initialize worker due to invalid credentials");
        }
    }

    private boolean validateWorkerCredentials() {
        String name = config.getWorkerName();
        String key = config.getWorkerKey();
        String uuid = config.getWorkerUUID();

        if (name == null || name.trim().isEmpty()) {
            logger.severe("Worker name is empty in config");
            return false;
        }
        if (key == null || key.trim().isEmpty()) {
            logger.severe("Worker key is empty in config");
            return false;
        }
        if (uuid == null || uuid.trim().isEmpty()) {
            logger.severe("Worker UUID is empty in config");
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
            logger.severe("Failed to check if worker exists: " + e.getMessage());
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
            logger.info("Worker registered successfully in database");
        } catch (SQLException e) {
            logger.severe("Failed to register worker: " + e.getMessage());
        }
    }
} 