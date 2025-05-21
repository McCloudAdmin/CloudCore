package systems.mythical.mccloudadmin.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import systems.mythical.mccloudadmin.config.DatabaseConfig;
import systems.mythical.mccloudadmin.utils.SafeAccountToken;

import java.sql.*;
import java.util.*;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private static UserService instance;
    private final DatabaseConfig databaseConfig;

    private UserService() {
        this.databaseConfig = DatabaseConfig.getInstance();
    }

    public static synchronized UserService getInstance() {
        if (instance == null) {
            instance = new UserService();
        }
        return instance;
    }

    public List<Map<String, Object>> listUsers() {
        List<Map<String, Object>> users = new ArrayList<>();
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users WHERE deleted = 'false'")) {
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(extractUserData(rs));
                }
            }
            return users;
        } catch (SQLException e) {
            logger.error("Error listing users: {}", e.getMessage());
            throw new RuntimeException("Failed to list users", e);
        }
    }

    public Map<String, Object> getUserById(int id) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users WHERE id = ? AND deleted = 'false'")) {
            
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserData(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error getting user by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public Map<String, Object> getUserByEmail(String email) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users WHERE email = ? AND deleted = 'false'")) {
            
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserData(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error getting user by email {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public Map<String, Object> getUserByUsername(String username) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users WHERE username = ? AND deleted = 'false'")) {
            
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserData(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error getting user by username {}: {}", username, e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public Map<String, Object> getUserByUuid(String uuid) {
        try (Connection conn = databaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM mccloudadmin_users WHERE uuid = ? AND deleted = 'false'")) {
            
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractUserData(rs);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error getting user by uuid {}: {}", uuid, e.getMessage());
            throw new RuntimeException("Failed to get user", e);
        }
    }

    public Map<String, Object> registerUser(Map<String, Object> userData) {
        try (Connection conn = databaseConfig.getConnection()) {
            // Check if username or uuid already exists
            if (getUserByUsername((String) userData.get("username")) != null) {
                throw new RuntimeException("Username already exists");
            }

            if (getUserByUuid((String) userData.get("uuid")) != null) {
                throw new RuntimeException("UUID already exists");
            }

            String sql = "INSERT INTO mccloudadmin_users (first_name, last_name, username, uuid, token, first_ip, last_ip) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int i = 1;
                stmt.setString(i++, (String) "Unknown");
                stmt.setString(i++, (String) "Unknown");
                stmt.setString(i++, (String) userData.get("username"));
                stmt.setString(i++, (String) userData.get("uuid"));
                stmt.setString(i++, SafeAccountToken.generateToken((String) userData.get("username")));
                stmt.setString(i++, (String) userData.get("first_ip"));
                stmt.setString(i++, (String) userData.get("last_ip"));
                stmt.executeUpdate();

                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return getUserById(generatedKeys.getInt(1));
                    }
                }
            }
            throw new RuntimeException("Failed to get generated user ID");
        } catch (SQLException e) {
            logger.error("Error registering user: {}", e.getMessage());
            throw new RuntimeException("Failed to register user", e);
        }
    }

    public Map<String, Object> updateUser(int id, Map<String, Object> userData) {
        try (Connection conn = databaseConfig.getConnection()) {
            // Build dynamic update query
            StringBuilder sql = new StringBuilder("UPDATE mccloudadmin_users SET ");
            List<Object> values = new ArrayList<>();
            
            for (Map.Entry<String, Object> entry : userData.entrySet()) {
                if (!entry.getKey().equals("id")) { // Skip id field
                    sql.append(entry.getKey()).append(" = ?, ");
                    values.add(entry.getValue());
                }
            }
            
            sql.setLength(sql.length() - 2); // Remove trailing comma and space
            sql.append(" WHERE id = ? AND deleted = 'false'");
            values.add(id);

            try (PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
                for (int i = 0; i < values.size(); i++) {
                    stmt.setObject(i + 1, values.get(i));
                }
                
                int affectedRows = stmt.executeUpdate();
                if (affectedRows > 0) {
                    return getUserById(id);
                }
            }
            return null;
        } catch (SQLException e) {
            logger.error("Error updating user {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    private Map<String, Object> extractUserData(ResultSet rs) throws SQLException {
        Map<String, Object> user = new HashMap<>();
        user.put("id", rs.getInt("id"));
        user.put("username", rs.getString("username"));
        user.put("first_name", rs.getString("first_name"));
        user.put("last_name", rs.getString("last_name"));
        user.put("email", rs.getString("email"));
        user.put("avatar", rs.getString("avatar"));
        user.put("credits", rs.getInt("credits"));
        user.put("background", rs.getString("background"));
        user.put("uuid", rs.getString("uuid"));
        user.put("token", rs.getString("token"));
        user.put("role", rs.getInt("role"));
        user.put("first_ip", rs.getString("first_ip"));
        user.put("last_ip", rs.getString("last_ip"));
        user.put("banned", rs.getString("banned"));
        user.put("verified", rs.getString("verified"));
        user.put("support_pin", rs.getString("support_pin"));
        user.put("2fa_enabled", rs.getString("2fa_enabled"));
        user.put("2fa_key", rs.getString("2fa_key"));
        user.put("2fa_blocked", rs.getString("2fa_blocked"));
        user.put("discord_id", rs.getString("discord_id"));
        user.put("github_id", rs.getObject("github_id"));
        user.put("github_username", rs.getString("github_username"));
        user.put("github_email", rs.getString("github_email"));
        user.put("github_linked", rs.getString("github_linked"));
        user.put("discord_username", rs.getString("discord_username"));
        user.put("discord_global_name", rs.getString("discord_global_name"));
        user.put("discord_email", rs.getString("discord_email"));
        user.put("discord_linked", rs.getString("discord_linked"));
        user.put("deleted", rs.getString("deleted"));
        user.put("locked", rs.getString("locked"));
        user.put("last_seen", rs.getTimestamp("last_seen"));
        user.put("first_seen", rs.getTimestamp("first_seen"));
        return user;
    }
} 