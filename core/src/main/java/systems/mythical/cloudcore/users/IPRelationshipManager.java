package systems.mythical.cloudcore.users;

import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class IPRelationshipManager {
    private static IPRelationshipManager instance;
    private final DatabaseManager databaseManager;
    private final Logger logger;

    private IPRelationshipManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public static IPRelationshipManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new IPRelationshipManager(databaseManager, logger);
        }
        return instance;
    }

    public boolean addIPRelationship(String userUuid, String ip) {
        try (Connection conn = databaseManager.getConnection()) {
            // First check if this IP relationship already exists
            String checkQuery = "SELECT id FROM mccloudadmin_ip_relationship WHERE user = ? AND ip = ? AND deleted = 'false'";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, userUuid);
                checkStmt.setString(2, ip);
                try (ResultSet rs = checkStmt.executeQuery()) {
                    if (rs.next()) {
                        // Relationship exists, update the timestamp
                        String updateQuery = "UPDATE mccloudadmin_ip_relationship SET updated_at = CURRENT_TIMESTAMP WHERE id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setInt(1, rs.getInt("id"));
                            return updateStmt.executeUpdate() > 0;
                        }
                    }
                }
            }

            // If no existing relationship, create new one
            String insertQuery = "INSERT INTO mccloudadmin_ip_relationship (user, ip) VALUES (?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                insertStmt.setString(1, userUuid);
                insertStmt.setString(2, ip);
                return insertStmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            logger.severe("Error managing IP relationship: " + e.getMessage());
            return false;
        }
    }

    public List<IPRelationship> getUserIPRelationships(String userUuid) {
        List<IPRelationship> relationships = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_ip_relationship WHERE user = ? AND deleted = 'false' ORDER BY updated_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userUuid);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        relationships.add(mapResultSetToRelationship(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting user IP relationships: " + e.getMessage());
        }
        return relationships;
    }

    public List<IPRelationship> getIPRelationships(String ip) {
        List<IPRelationship> relationships = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_ip_relationship WHERE ip = ? AND deleted = 'false' ORDER BY updated_at DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, ip);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        relationships.add(mapResultSetToRelationship(rs));
                    }
                }
            }
        } catch (SQLException e) {
            logger.severe("Error getting IP relationships: " + e.getMessage());
        }
        return relationships;
    }

    private IPRelationship mapResultSetToRelationship(ResultSet rs) throws SQLException {
        IPRelationship relationship = new IPRelationship();
        relationship.setId(rs.getInt("id"));
        relationship.setUser(rs.getString("user"));
        relationship.setIp(rs.getString("ip"));
        relationship.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        relationship.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        relationship.setDeleted(rs.getString("deleted").equals("true"));
        relationship.setLocked(rs.getString("locked").equals("true"));
        return relationship;
    }
} 