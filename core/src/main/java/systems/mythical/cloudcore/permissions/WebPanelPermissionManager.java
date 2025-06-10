package systems.mythical.cloudcore.permissions;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class WebPanelPermissionManager {
    private static WebPanelPermissionManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    private WebPanelPermissionManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static WebPanelPermissionManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new WebPanelPermissionManager(databaseManager, logger);
        }
        return instance;
    }

    public void updateUserPermissions(UUID uuid, List<String> permissions, List<String> negativePermissions) {
        try (Connection conn = databaseManager.getConnection()) {
            // First, mark all existing non-locked permissions as deleted
            String updateQuery = """
                UPDATE mccloudadmin_users_permissions 
                SET deleted = 'true', updated_at = CURRENT_TIMESTAMP 
                WHERE uuid = ? AND locked = 'false'
            """;
            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                updateStmt.setString(1, uuid.toString());
                updateStmt.executeUpdate();
            }

            // Then insert/update the positive permissions
            String upsertQuery = """
                INSERT INTO mccloudadmin_users_permissions 
                (uuid, permission, granted, locked, deleted) 
                VALUES (?, ?, 'true', 'false', 'false')
                ON DUPLICATE KEY UPDATE 
                granted = 'true',
                locked = 'false',
                deleted = 'false',
                updated_at = CURRENT_TIMESTAMP
            """;
            try (PreparedStatement insertStmt = conn.prepareStatement(upsertQuery)) {
                for (String permission : permissions) {
                    insertStmt.setString(1, uuid.toString());
                    insertStmt.setString(2, permission);
                    insertStmt.executeUpdate(); // Execute one by one to handle duplicates better
                }
            }

            // Insert/update the negative permissions
            upsertQuery = """
                INSERT INTO mccloudadmin_users_permissions 
                (uuid, permission, granted, locked, deleted) 
                VALUES (?, ?, 'false', 'false', 'false')
                ON DUPLICATE KEY UPDATE 
                granted = 'false',
                locked = 'false',
                deleted = 'false',
                updated_at = CURRENT_TIMESTAMP
            """;
            try (PreparedStatement insertStmt = conn.prepareStatement(upsertQuery)) {
                for (String permission : negativePermissions) {
                    insertStmt.setString(1, uuid.toString());
                    insertStmt.setString(2, permission);
                    insertStmt.executeUpdate(); // Execute one by one to handle duplicates better
                }
            }
        } catch (SQLException e) {
            logger.severe("Error updating web panel permissions for user " + uuid + ": " + e.getMessage());
        }
    }

    public List<String> getUserPermissions(UUID uuid) {
        List<String> permissions = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = """
                SELECT permission 
                FROM mccloudadmin_users_permissions 
                WHERE uuid = ? 
                AND granted = 'true' 
                AND deleted = 'false'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        permissions.add(rs.getString("permission"));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting web panel permissions for user " + uuid + ": " + e.getMessage());
        }
        return permissions;
    }

    public boolean hasPermission(UUID uuid, String permission) {
        try (Connection conn = databaseManager.getConnection()) {
            // First check if the permission is explicitly denied
            String query = """
                SELECT COUNT(*) 
                FROM mccloudadmin_users_permissions 
                WHERE uuid = ? 
                AND permission = ? 
                AND granted = 'false' 
                AND deleted = 'false'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, permission);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Permission explicitly denied
                    }
                }
            }

            // Check if admin permission is explicitly denied
            query = """
                SELECT COUNT(*) 
                FROM mccloudadmin_users_permissions 
                WHERE uuid = ? 
                AND permission = ? 
                AND granted = 'false' 
                AND deleted = 'false'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, Permissions.PANEL_ADMIN);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return false; // Admin permission explicitly denied
                    }
                }
            }

            // Check for the specific permission
            query = """
                SELECT COUNT(*) 
                FROM mccloudadmin_users_permissions 
                WHERE uuid = ? 
                AND permission = ? 
                AND granted = 'true' 
                AND deleted = 'false'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, permission);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) {
                        return true;
                    }
                }
            }

            // Check for admin permission which grants all permissions
            query = """
                SELECT COUNT(*) 
                FROM mccloudadmin_users_permissions 
                WHERE uuid = ? 
                AND permission = ? 
                AND granted = 'true' 
                AND deleted = 'false'
            """;
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                stmt.setString(2, Permissions.PANEL_ADMIN);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next() && rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.severe("Error checking web panel permission for user " + uuid + ": " + e.getMessage());
        }
        return false;
    }
} 