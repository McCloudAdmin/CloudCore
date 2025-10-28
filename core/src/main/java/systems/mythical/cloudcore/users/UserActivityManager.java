package systems.mythical.cloudcore.users;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class UserActivityManager {
    private static UserActivityManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;

    private UserActivityManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
    }

    public static UserActivityManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new UserActivityManager(databaseManager, logger);
        }
        return instance;
    }

    public boolean logActivity(String userUuid, String action, String ipAddress, String context) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_users_activities (user, action, ip_address, context) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userUuid);
                stmt.setString(2, action);
                stmt.setString(3, ipAddress);
                stmt.setString(4, context);

                int result = stmt.executeUpdate();
                return result > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error logging user activity: " + e.getMessage());
            return false;
        }
    }

    public List<UserActivity> getUserActivities(String userUuid) {
        List<UserActivity> activities = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users_activities WHERE user = ? AND deleted = 'false' ORDER BY date DESC";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, userUuid);
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        activities.add(mapResultSetToActivity(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting user activities: " + e.getMessage());
        }
        return activities;
    }

    private UserActivity mapResultSetToActivity(ResultSet rs) throws SQLException {
        UserActivity activity = new UserActivity();
        activity.setId(rs.getInt("id"));
        activity.setUser(rs.getString("user"));
        activity.setAction(rs.getString("action"));
        activity.setIpAddress(rs.getString("ip_address"));
        activity.setDeleted(rs.getString("deleted").equals("true"));
        activity.setLocked(rs.getString("locked").equals("true"));
        activity.setDate(rs.getTimestamp("date").toLocalDateTime());
        activity.setContext(rs.getString("context"));
        return activity;
    }
} 