package systems.mythical.cloudcore.maintenance;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

public class MaintenanceSystemManager {
    private static MaintenanceSystemManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    private MaintenanceSystemManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static MaintenanceSystemManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new MaintenanceSystemManager(databaseManager, logger);
        }
        return instance;
    }

    public boolean isInMaintenance(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT id FROM mccloudadmin_maintenance_system WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking maintenance status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean addMaintenance(UUID uuid) {
        // First check if the UUID already exists
        if (isInMaintenance(uuid)) {
            logger.info("UUID " + uuid + " is already in maintenance list, skipping...");
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_maintenance_system (uuid) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                logger.info("Added maintenance entry for: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error adding maintenance entry for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean removeMaintenance(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "DELETE FROM mccloudadmin_maintenance_system WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                logger.info("Removed maintenance entry for: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            logger.severe("Error removing maintenance entry for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public String getMaintenanceList() {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT uuid FROM mccloudadmin_maintenance_system ORDER BY id";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                 ResultSet rs = stmt.executeQuery()) {
                StringBuilder list = new StringBuilder();
                boolean first = true;
                while (rs.next()) {
                    if (!first) {
                        list.append(",");
                    }
                    list.append(rs.getString("uuid"));
                    first = false;
                }
                return list.toString();
            }
        } catch (SQLException e) {
            logger.severe("Error getting maintenance list: " + e.getMessage());
            return "";
        }
    }
} 