package systems.mythical.cloudcore.users;

import systems.mythical.cloudcore.core.CloudCoreLogic;
import systems.mythical.cloudcore.database.DatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class UserManager {
    private static UserManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger = CloudLoggerFactory.get();

    private UserManager(DatabaseManager databaseManager, Logger logger) {
        this.databaseManager = databaseManager;
    }

    public static UserManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new UserManager(databaseManager, logger);
        }
        return instance;
    }

    public User createUser(String username, UUID uuid, String ip) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "INSERT INTO mccloudadmin_users (username, first_ip, last_ip, uuid, token, user_connected_server_name, user_online, userGroup, support_pin, avatar) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            String token = CloudCoreLogic.generateSecureStringToken(username, uuid.toString());
            int supportPin = CloudCoreLogic.generateRandomNumber(100000, 999999);

            try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, username);
                stmt.setString(2, ip);
                stmt.setString(3, ip);
                stmt.setString(4, uuid.toString());
                stmt.setString(5, token);
                stmt.setString(6, "lobby"); // Default connected server
                stmt.setString(7, "true"); // Default online status
                stmt.setString(8, "default"); // Default user group
                stmt.setInt(9, supportPin);
                stmt.setString(10, "https://mc-heads.net/avatar/"+username);
                stmt.executeUpdate();

                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt(1));
                        user.setUsername(username);
                        user.setFirstIp(ip);
                        user.setLastIp(ip);
                        user.setUuid(uuid);
                        user.setToken(token);
                        user.setVerified(false);
                        user.setAvatar("https://mc-heads.net/avatar/"+username);
                        user.setBackground("https://cdn.mythical.systems/background.gif");
                        user.setFirstSeen(LocalDateTime.now());
                        user.setLastSeen(LocalDateTime.now());
                        user.setUserConnectedServerName("lobby");
                        user.setUserOnline(true);
                        user.setUserGroup("default");
                        user.setSupportPin(String.valueOf(supportPin));
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error creating user: " + e.getMessage());
        }
        return null;
    }

    public Optional<User> getUserById(int id) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users WHERE id = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting user by ID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUuid(UUID uuid) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users WHERE uuid = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, uuid.toString());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting user by UUID: " + e.getMessage());
        }
        return Optional.empty();
    }

    public Optional<User> getUserByUsername(String username) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users WHERE username = ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, username);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return Optional.of(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting user by username: " + e.getMessage());
        }
        return Optional.empty();
    }

    public boolean updateUser(User user) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_users SET " +
                    "username = ?, first_name = ?, last_name = ?, email = ?, " +
                    "avatar = ?, credits = ?, background = ?, " +
                    "last_ip = ?, verified = ?, support_pin = ?, " +
                    "2fa_enabled = ?, 2fa_key = ?, 2fa_blocked = ?, " +
                    "discord_id = ?, github_id = ?, github_username = ?, " +
                    "github_email = ?, github_linked = ?, discord_username = ?, " +
                    "discord_global_name = ?, discord_email = ?, discord_linked = ?, " +
                    "locked = ?, last_seen = ?, user_version = ?, " +
                    "user_client_name = ?, user_connected_server_name = ?, user_online = ?, userGroup = ?, " +
                    "banned = ?, ban_reason = ?, userGroupWeight = ? " +
                    "WHERE id = ?";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int paramIndex = 1;
                stmt.setString(paramIndex++, user.getUsername());
                stmt.setString(paramIndex++, user.getFirstName());
                stmt.setString(paramIndex++, user.getLastName());
                stmt.setString(paramIndex++, user.getEmail());
                stmt.setString(paramIndex++, user.getAvatar());
                stmt.setInt(paramIndex++, user.getCredits());
                stmt.setString(paramIndex++, user.getBackground());
                stmt.setString(paramIndex++, user.getLastIp());
                stmt.setString(paramIndex++, user.isVerified() ? "true" : "false");
                stmt.setString(paramIndex++, user.getSupportPin());
                stmt.setString(paramIndex++, user.isTwoFactorEnabled() ? "true" : "false");
                stmt.setString(paramIndex++, user.getTwoFactorKey());
                stmt.setString(paramIndex++, user.isTwoFactorBlocked() ? "true" : "false");
                stmt.setString(paramIndex++, user.getDiscordId());
                stmt.setObject(paramIndex++, user.getGithubId());
                stmt.setString(paramIndex++, user.getGithubUsername());
                stmt.setString(paramIndex++, user.getGithubEmail());
                stmt.setString(paramIndex++, user.isGithubLinked() ? "true" : "false");
                stmt.setString(paramIndex++, user.getDiscordUsername());
                stmt.setString(paramIndex++, user.getDiscordGlobalName());
                stmt.setString(paramIndex++, user.getDiscordEmail());
                stmt.setString(paramIndex++, user.isDiscordLinked() ? "true" : "false");
                stmt.setString(paramIndex++, user.isLocked() ? "true" : "false");
                stmt.setTimestamp(paramIndex++, Timestamp.valueOf(user.getLastSeen()));
                stmt.setString(paramIndex++, user.getUserVersion());
                stmt.setString(paramIndex++, user.getUserClientName());
                stmt.setString(paramIndex++, user.getUserConnectedServerName());
                stmt.setString(paramIndex++, user.isUserOnline() ? "true" : "false");
                stmt.setString(paramIndex++, user.getUserGroup());
                stmt.setString(paramIndex++, user.isBanned() ? "true" : "false");
                stmt.setString(paramIndex++, user.getBanReason());
                stmt.setInt(paramIndex++, user.getUserGroupWeight());
                stmt.setInt(paramIndex, user.getId());

                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error updating user: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteUser(int id) {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_users SET deleted = 'true' WHERE id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, id);
                return stmt.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users WHERE deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query);
                    ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    users.add(mapResultSetToUser(rs));
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting all users: " + e.getMessage());
            return new ArrayList<>();
        }
        return users;
    }

    public List<User> getOtherAccounts(User user) {
        List<User> otherAccounts = new ArrayList<>();
        try (Connection conn = databaseManager.getConnection()) {
            String query = "SELECT * FROM mccloudadmin_users WHERE (first_ip = ? OR last_ip = ?) AND id != ? AND deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setString(1, user.getFirstIp());
                stmt.setString(2, user.getLastIp());
                stmt.setInt(3, user.getId());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        otherAccounts.add(mapResultSetToUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            cloudLogger.error("Error getting other accounts: " + e.getMessage());
            return new ArrayList<>();
        }
        return otherAccounts;
    }

    public boolean markAllUsersOffline() {
        try (Connection conn = databaseManager.getConnection()) {
            String query = "UPDATE mccloudadmin_users SET user_online = 'false' WHERE deleted = 'false'";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                int updated = stmt.executeUpdate();
                cloudLogger.info("Marked " + updated + " users as offline");
                return updated > 0;
            }
        } catch (SQLException e) {
            cloudLogger.error("Error marking users as offline: " + e.getMessage());
            return false;
        }
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setUserVersion(rs.getString("user_version"));
        user.setUserClientName(rs.getString("user_client_name"));
        user.setUserConnectedServerName(rs.getString("user_connected_server_name"));
        user.setUserOnline(rs.getString("user_online").equals("true"));
        user.setEmail(rs.getString("email"));
        user.setAvatar(rs.getString("avatar"));
        user.setCredits(rs.getInt("credits"));
        user.setBackground(rs.getString("background"));
        user.setUuid(UUID.fromString(rs.getString("uuid")));
        user.setToken(rs.getString("token"));
        user.setFirstIp(rs.getString("first_ip"));
        user.setLastIp(rs.getString("last_ip"));
        user.setVerified(rs.getString("verified").equals("true"));
        user.setSupportPin(rs.getString("support_pin"));
        user.setTwoFactorEnabled(rs.getString("2fa_enabled").equals("true"));
        user.setTwoFactorKey(rs.getString("2fa_key"));
        user.setTwoFactorBlocked(rs.getString("2fa_blocked").equals("true"));
        user.setDiscordId(rs.getString("discord_id"));
        user.setGithubId(rs.getInt("github_id"));
        user.setGithubUsername(rs.getString("github_username"));
        user.setGithubEmail(rs.getString("github_email"));
        user.setGithubLinked(rs.getString("github_linked").equals("true"));
        user.setDiscordUsername(rs.getString("discord_username"));
        user.setDiscordGlobalName(rs.getString("discord_global_name"));
        user.setDiscordEmail(rs.getString("discord_email"));
        user.setDiscordLinked(rs.getString("discord_linked").equals("true"));
        user.setDeleted(rs.getString("deleted").equals("true"));
        user.setLocked(rs.getString("locked").equals("true"));
        user.setLastSeen(rs.getTimestamp("last_seen").toLocalDateTime());
        user.setFirstSeen(rs.getTimestamp("first_seen").toLocalDateTime());
        user.setBanned(rs.getString("banned").equals("true"));
        user.setBanReason(rs.getString("ban_reason"));
        user.setUserGroup(rs.getString("userGroup"));
        user.setUserGroupWeight(rs.getInt("userGroupWeight"));
        return user;
    }
}