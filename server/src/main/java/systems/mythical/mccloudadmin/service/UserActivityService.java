package systems.mythical.mccloudadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.DatabaseConfig;

import java.sql.*;
import java.util.*;

public class UserActivityService {
    private static final Logger logger = LoggerFactory.getLogger(UserActivityService.class);
    private static UserActivityService instance;
    private final DatabaseConfig databaseConfig;

    private UserActivityService() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    public static synchronized UserActivityService getInstance() {
        if (instance == null) {
            instance = new UserActivityService();
        }
        return instance;
    }

    public List<Map<String, Object>> listActivities() {
        List<Map<String, Object>> activities = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users_activities WHERE deleted = 'false' ORDER BY date DESC")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(extractActivityData(rs));
                }
            }
            return activities;
        } catch (SQLException e) {
            logger.error("Error listing activities: {}", e.getMessage());
            throw new RuntimeException("Failed to list activities", e);
        }
    }

    public List<Map<String, Object>> getActivitiesByUser(String userUuid) {
        List<Map<String, Object>> activities = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users_activities WHERE user = ? AND deleted = 'false' ORDER BY date DESC")) {
            
            stmt.setString(1, userUuid);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(extractActivityData(rs));
                }
            }
            return activities;
        } catch (SQLException e) {
            logger.error("Error getting activities for user {}: {}", userUuid, e.getMessage());
            throw new RuntimeException("Failed to get user activities", e);
        }
    }

    public Map<String, Object> addActivity(String userUuid, String action, String ipAddress, String context) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO mccloudadmin_users_activities (user, action, ip_address, context) VALUES (?, ?, ?, ?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            
            stmt.setString(1, userUuid);
            stmt.setString(2, action);
            stmt.setString(3, ipAddress);
            stmt.setString(4, context != null ? context : "None");

            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return getActivityById(generatedKeys.getInt(1));
                }
            }
            throw new RuntimeException("Failed to get generated activity ID");
        } catch (SQLException e) {
            logger.error("Error adding activity: {}", e.getMessage());
            throw new RuntimeException("Failed to add activity", e);
        }
    }

    public Map<String, Object> getActivityById(int id) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users_activities WHERE id = ? AND deleted = 'false'")) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractActivityData(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error getting activity by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get activity", e);
        }
    }

    public Map<String, Object> updateActivity(int id, Map<String, Object> activityData) {
        try (Connection conn = databaseConfig.getConnection()) {
            StringBuilder sql = new StringBuilder("UPDATE mccloudadmin_users_activities SET ");
            List<Object> values = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : activityData.entrySet()) {
                if (!entry.getKey().equals("id")) {
                    sql.append(entry.getKey()).append(" = ?, ");
                    values.add(entry.getValue());
                }
            }
            
            sql.setLength(sql.length() - 2);
            sql.append(" WHERE id = ? AND deleted = 'false'");
            values.add(id);

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    return getActivityById(id);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error updating activity {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update activity", e);
        }
    }

    private Map<String, Object> extractActivityData(ResultSet rs) throws SQLException {
        Map<String, Object> activity = new HashMap<>();
        activity.put("id", rs.getInt("id"));
        activity.put("user", rs.getString("user"));
        activity.put("action", rs.getString("action"));
        activity.put("ip_address", rs.getString("ip_address"));
        activity.put("deleted", rs.getString("deleted"));
        activity.put("locked", rs.getString("locked"));
        activity.put("date", rs.getTimestamp("date"));
        activity.put("context", rs.getString("context"));
        return activity;
    }
} 