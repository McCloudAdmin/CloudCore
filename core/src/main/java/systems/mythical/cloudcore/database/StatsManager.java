package systems.mythical.cloudcore.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class StatsManager {
    private final DatabaseManager databaseManager;
    private final Logger logger;

    public StatsManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
        this.logger = logger;
    }

    public boolean createStat(String user, String worker, String type, String value) {
        String sql = "INSERT INTO mccloudadmin_stats (user, worker, type, value) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            stmt.setString(4, value);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to create stat: " + e.getMessage());
            return false;
        }
    }

    public Optional<String> getStat(String user, String worker, String type) {
        String sql = "SELECT value FROM mccloudadmin_stats WHERE user = ? AND worker = ? AND type = ? AND deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.ofNullable(rs.getString("value"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to get stat: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean updateStat(String user, String worker, String type, String value) {
        String sql = "UPDATE mccloudadmin_stats SET value = ?, updated_at = CURRENT_TIMESTAMP WHERE user = ? AND worker = ? AND type = ? AND deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, value);
            stmt.setString(2, user);
            stmt.setString(3, worker);
            stmt.setString(4, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Failed to update stat: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteStat(String user, String worker, String type) {
        String sql = "UPDATE mccloudadmin_stats SET deleted = 'true', updated_at = CURRENT_TIMESTAMP WHERE user = ? AND worker = ? AND type = ?";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            logger.severe("Failed to delete stat: " + e.getMessage());
            return false;
        }
    }

    public List<String> listStatsByUser(String user) {
        List<String> stats = new ArrayList<>();
        String sql = "SELECT type, value FROM mccloudadmin_stats WHERE user = ? AND deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(rs.getString("type") + ":" + rs.getString("value"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to list stats by user: " + e.getMessage());
        }
        return stats;
    }

    public List<String> listStatsByWorker(String worker) {
        List<String> stats = new ArrayList<>();
        String sql = "SELECT type, value FROM mccloudadmin_stats WHERE worker = ? AND deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, worker);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(rs.getString("type") + ":" + rs.getString("value"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to list stats by worker: " + e.getMessage());
        }
        return stats;
    }

    public List<String> listStatsByType(String type) {
        List<String> stats = new ArrayList<>();
        String sql = "SELECT user, worker, value FROM mccloudadmin_stats WHERE type = ? AND deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, type);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    stats.add(rs.getString("user") + ":" + rs.getString("worker") + ":" + rs.getString("value"));
                }
            }
        } catch (SQLException e) {
            logger.severe("Failed to list stats by type: " + e.getMessage());
        }
        return stats;
    }

    public boolean setOrUpdateStat(String user, String worker, String type, String value) {
        String sql = "INSERT INTO mccloudadmin_stats (user, worker, type, value) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = VALUES(value), updated_at = CURRENT_TIMESTAMP, deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            stmt.setString(4, value);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to set or update stat: " + e.getMessage());
            return false;
        }
    }

    public boolean setOrUpdateStatByOne(String user, String worker, String type) {
        String sql = "INSERT INTO mccloudadmin_stats (user, worker, type, value) VALUES (?, ?, ?, '1') " +
                     "ON DUPLICATE KEY UPDATE value = CAST(value AS UNSIGNED) + 1, updated_at = CURRENT_TIMESTAMP, deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to increment or create stat: " + e.getMessage());
            return false;
        }
    }

    public boolean incrementStatBy(String user, String worker, String type, int amount) {
        String sql = "INSERT INTO mccloudadmin_stats (user, worker, type, value) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = CAST(value AS UNSIGNED) + VALUES(value), updated_at = CURRENT_TIMESTAMP, deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            stmt.setString(4, Integer.toString(amount));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to increment stat by amount: " + e.getMessage());
            return false;
        }
    }

    public boolean setStat(String user, String worker, String type, double value) {
        String sql = "INSERT INTO mccloudadmin_stats (user, worker, type, value) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE value = VALUES(value), updated_at = CURRENT_TIMESTAMP, deleted = 'false'";
        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user);
            stmt.setString(2, worker);
            stmt.setString(3, type);
            stmt.setString(4, Double.toString(value));
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            logger.severe("Failed to set stat: " + e.getMessage());
            return false;
        }
    }
} 