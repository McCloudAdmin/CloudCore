package systems.mythical.cloudcore.settings;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class CloudSettings {
    private static CloudSettings instance;
    private final Map<String, String> settingsCache;
    private final DatabaseManager databaseManager;
    private final Logger logger;
    private final ScheduledExecutorService scheduler;

    private CloudSettings(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
        this.settingsCache = new HashMap<>();
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        
        // Initial load
        refreshSettings();
        
        // Schedule refresh every 15 minutes
        scheduler.scheduleAtFixedRate(this::refreshSettings, 15, 15, TimeUnit.MINUTES);
    }

    public static CloudSettings getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new CloudSettings(databaseManager, logger);
        }
        return instance;
    }

    public void refreshSettings() {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT name, value FROM mccloudadmin_settings WHERE deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                
                Map<String, String> newCache = new HashMap<>();
                while (rs.next()) {
                    newCache.put(rs.getString("name"), rs.getString("value"));
                }
                
                synchronized (settingsCache) {
                    settingsCache.clear();
                    settingsCache.putAll(newCache);
                }
                
                logger.info("Settings cache refreshed successfully");
            }
        } catch (SQLException e) {
            logger.severe("Error refreshing settings cache: " + e.getMessage());
        }
    }

    public String getSetting(String name) {
        synchronized (settingsCache) {
            return settingsCache.getOrDefault(name, "");
        }
    }

    public boolean settingExists(String name) {
        synchronized (settingsCache) {
            return settingsCache.containsKey(name);
        }
    }

    public void setSetting(String name, String value) {
        try (Connection conn = databaseManager.getConnection()) {
            // First check if the setting exists
            String checkQuery = "SELECT id FROM mccloudadmin_settings WHERE name = ? AND deleted = 'false'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, name);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Setting exists, update it
                        String updateQuery = "UPDATE mccloudadmin_settings SET value = ? WHERE name = ? AND deleted = 'false'";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, value);
                            updateStmt.setString(2, name);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // Setting doesn't exist, insert it
                        String insertQuery = "INSERT INTO mccloudadmin_settings (name, value) VALUES (?, ?)";
                        try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                            insertStmt.setString(1, name);
                            insertStmt.setString(2, value);
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            
            synchronized (settingsCache) {
                settingsCache.put(name, value);
            }
            logger.info("Successfully set value for setting: " + name);
        } catch (SQLException e) {
            logger.severe("Error setting value for " + name + ": " + e.getMessage());
        }
    }

    public void deleteSetting(String name) {
        try (Connection conn = databaseManager.getConnection()) {
            // First check if the setting exists
            String checkQuery = "SELECT id FROM mccloudadmin_settings WHERE name = ? AND deleted = 'false'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, name);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Setting exists, mark it as deleted
                        String updateQuery = "UPDATE mccloudadmin_settings SET deleted = 'true' WHERE name = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, name);
                            updateStmt.executeUpdate();
                            
                            synchronized (settingsCache) {
                                settingsCache.remove(name);
                            }
                            logger.info("Successfully deleted setting: " + name);
                        }
                    } else {
                        logger.info("Setting " + name + " does not exist, skipping delete");
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error deleting setting " + name + ": " + e.getMessage());
        }
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