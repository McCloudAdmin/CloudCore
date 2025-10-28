package systems.mythical.cloudcore.users;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.logging.Logger;

public class SystemUserManager {
    private static SystemUserManager instance;
    private final DatabaseManager databaseManager;
    private final CloudLogger cloudLogger;
    private final UserManager userManager;

    private SystemUserManager(DatabaseManager databaseManager, Logger platformLogger) {
        this.databaseManager = databaseManager;
        this.cloudLogger = CloudLoggerFactory.get();
        this.userManager = UserManager.getInstance(databaseManager, platformLogger);
    }

    public static SystemUserManager getInstance(DatabaseManager databaseManager, Logger logger) {
        if (instance == null) {
            instance = new SystemUserManager(databaseManager, logger);
        }
        return instance;
    }

    public void createSystemUserIfNotExists() {
        try {
            // Check if system user already exists
            Optional<User> existingUser = userManager.getUserById(0);
            if (existingUser.isPresent()) {
                cloudLogger.debug("System user already exists");
                return;
            }

            // Create the system user
            createSystemUser();
            cloudLogger.info("System user created successfully");
        } catch (Exception e) {
            cloudLogger.error("Error creating system user: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createSystemUser() throws SQLException {
        String insertSQL = """
            INSERT INTO `mccloudadmin_users` (
                `id`, `username`, `first_name`, `last_name`, `user_version`, `user_client_name`, 
                `user_connected_server_name`, `user_online`, `email`, `avatar`, `credits`, 
                `background`, `uuid`, `token`, `first_ip`, `last_ip`, `verified`, `support_pin`, 
                `2fa_enabled`, `2fa_key`, `2fa_blocked`, `discord_id`, `github_id`, `github_username`, 
                `github_email`, `github_linked`, `discord_username`, `discord_global_name`, 
                `discord_email`, `discord_linked`, `userGroup`, `userGroupWeight`, `deleted`, 
                `locked`, `last_seen`, `first_seen`, `banned`, `ban_reason`
            ) VALUES (
                -1, 'CloudAdmin', 'Cloud', 'Admin', 'None', 'None', 'None', 'false', 
                'system@mythical.systems', 'https://www.gravatar.com/avatar', 0, 
                'https://github.com/mythicalltd.png', '00000000-0000-0000-0000-000000000000', 
                'SYSTEM_TOKEN', '127.0.0.1', '127.0.0.1', 'false', '000000', 'true', NULL, 
                'true', NULL, NULL, NULL, NULL, 'false', NULL, NULL, NULL, 'false', 
                'system', -1, 'false', 'false', ?, ?, 'true', NULL
            ) ON DUPLICATE KEY UPDATE id = id
            """;

        try (Connection conn = databaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {
            
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setTimestamp(2, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            
            stmt.executeUpdate();
        }
    }

    public User getSystemUser() {
        Optional<User> systemUser = userManager.getUserById(0);
        if (systemUser.isEmpty()) {
            createSystemUserIfNotExists();
            return userManager.getUserById(0).orElse(null);
        }
        return systemUser.get();
    }
} 