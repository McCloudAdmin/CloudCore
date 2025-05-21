package systems.mythical.mccloudadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.DatabaseConfig;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SettingsService {
    private static final Logger logger = LoggerFactory.getLogger(SettingsService.class);
    private static SettingsService instance;
    private final DatabaseConfig databaseConfig;
    private final Map<String, String> settingsCache = new ConcurrentHashMap<>();
    private volatile long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 5000; // 5 seconds cache TTL

    private SettingsService() {
        this.databaseConfig = DatabaseConfig.getInstance();
        checkSettingsTable();
        refreshCache();
    }

    public static synchronized SettingsService getInstance() {
        if (instance == null) {
            instance = new SettingsService();
        }
        return instance;
    }

    private void checkSettingsTable() {
        try (Connection conn = databaseConfig.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tables = metaData.getTables(null, null, "mccloudadmin_settings", new String[]{"TABLE"})) {
                if (!tables.next()) {
                    logger.error("Table 'mccloudadmin_settings' not found. Migrations were not executed on panel side.");
                    System.exit(1);
                }
            }
        } catch (SQLException e) {
            logger.error("Error checking settings table: {}", e.getMessage());
            System.exit(1);
        }
    }

    private void refreshCache() {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT name, value FROM mccloudadmin_settings WHERE deleted = 'false'")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                Map<String, String> newCache = new HashMap<>();
                while (rs.next()) {
                    newCache.put(rs.getString("name"), rs.getString("value"));
                }
                settingsCache.clear();
                settingsCache.putAll(newCache);
                lastCacheUpdate = System.currentTimeMillis();
            }
        } catch (SQLException e) {
            logger.error("Error refreshing settings cache: {}", e.getMessage());
        }
    }

    private void checkAndRefreshCache() {
        if (System.currentTimeMillis() - lastCacheUpdate > CACHE_TTL) {
            refreshCache();
        }
    }

    public String getSetting(String name) {
        checkAndRefreshCache();
        return settingsCache.get(name);
    }

    public void setSetting(String name, String value) {
        try (Connection conn = databaseConfig.getConnection()) {
            // Check if setting exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                    "SELECT id FROM mccloudadmin_settings WHERE name = ? AND deleted = 'false'")) {
                checkStmt.setString(1, name);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Update existing setting
                        try (PreparedStatement updateStmt = conn.prepareStatement(
                                "UPDATE mccloudadmin_settings SET value = ?, date = ? WHERE name = ? AND deleted = 'false'")) {
                            updateStmt.setString(1, value);
                            updateStmt.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
                            updateStmt.setString(3, name);
                            updateStmt.executeUpdate();
                        }
                    } else {
                        // Insert new setting
                        try (PreparedStatement insertStmt = conn.prepareStatement(
                                "INSERT INTO mccloudadmin_settings (name, value, locked, deleted, date) VALUES (?, ?, 'false', 'false', ?)")) {
                            insertStmt.setString(1, name);
                            insertStmt.setString(2, value);
                            insertStmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                            insertStmt.executeUpdate();
                        }
                    }
                }
            }
            // Update cache immediately
            settingsCache.put(name, value);
            lastCacheUpdate = System.currentTimeMillis();
        } catch (SQLException e) {
            logger.error("Error setting {} to {}: {}", name, value, e.getMessage());
            throw new RuntimeException("Failed to set setting", e);
        }
    }

    public Map<String, String> getAllSettings() {
        checkAndRefreshCache();
        return new HashMap<>(settingsCache);
    }

    public void deleteSetting(String name) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE mccloudadmin_settings SET deleted = 'true', date = ? WHERE name = ? AND deleted = 'false'")) {
            
            stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, name);
            stmt.executeUpdate();
            
            // Update cache immediately
            settingsCache.remove(name);
            lastCacheUpdate = System.currentTimeMillis();
        } catch (SQLException e) {
            logger.error("Error deleting setting {}: {}", name, e.getMessage());
            throw new RuntimeException("Failed to delete setting", e);
        }
    }
} 