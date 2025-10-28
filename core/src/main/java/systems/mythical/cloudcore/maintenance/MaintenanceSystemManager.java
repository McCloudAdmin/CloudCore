package systems.mythical.cloudcore.maintenance;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.*;
import java.util.UUID;
import java.util.logging.Logger;

public class MaintenanceSystemManager {
    private static MaintenanceSystemManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;

    private MaintenanceSystemManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
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
            cloudLogger.error("Error checking maintenance status for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean addMaintenance(UUID uuid) {
        // First check if the UUID already exists
        if (isInMaintenance(uuid)) {
            cloudLogger.debug("UUID " + uuid + " is already in maintenance list, skipping...");
            return false;
        }

        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_maintenance_system (uuid) VALUES (?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Added maintenance entry for: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error adding maintenance entry for " + uuid + ": " + e.getMessage());
            return false;
        }
    }

    public boolean removeMaintenance(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "DELETE FROM mccloudadmin_maintenance_system WHERE uuid = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();
                cloudLogger.info("Removed maintenance entry for: " + uuid);
                return true;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error removing maintenance entry for " + uuid + ": " + e.getMessage());
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
            cloudLogger.error("Error getting maintenance list: " + e.getMessage());
            return "";
        }
    }
}
