package systems.mythical.cloudcore.events;

import systems.mythical.cloudcore.commands.CommandLogManager;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.permissions.PermissionChecker;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.CommonSettings;

import java.util.UUID;
import java.util.logging.Logger;
import java.util.Set;
import java.util.HashSet;

public class CommandEvent {
    private static final Logger logger = Logger.getLogger(CommandEvent.class.getName());
    private static CommandLogManager commandLogManager;
    private static PermissionChecker permissionChecker;
    private static CloudSettings cloudSettings;

    // Permission nodes
    public static final String COMMAND_BYPASS_PERMISSION = "cloudcore.command.bypass";

    // Settings
    private static final CommonSettings.BooleanSetting LOG_COMMAND_LOGS = new CommonSettings.BooleanSetting("log_command_logs", true);

    // Sensitive commands that should never be logged
    private static final Set<String> SENSITIVE_COMMANDS = new HashSet<>(Set.of(
        "login", "register", "l", "reg", "changepassword", "changepass", "pass", "password",
        "2fa", "2fasetup", "2faconfirm", "auth", "authenticate", "verify", "verification"
    ));

    public static void initialize(DatabaseManager databaseManager) {
        commandLogManager = CommandLogManager.getInstance(databaseManager, logger);
        permissionChecker = PermissionChecker.getInstance();
        cloudSettings = CloudSettings.getInstance(databaseManager, logger);
    }

    /**
     * Handles a command event
     *
     * @param uuid The UUID of the player who executed the command
     * @param content The full command string (including the /)
     * @param server The server where the command was executed
     * @return true if the command should be allowed, false if it should be blocked
     */
    public static boolean onPlayerCommand(UUID uuid, String content, String server) {
        if (commandLogManager == null || permissionChecker == null || cloudSettings == null) {
            logger.severe("Managers not initialized! Call CommandEvent.initialize() first.");
            return true; // Allow the command if not initialized
        }

        try {
            // Check if command logging is enabled
            if (!LOG_COMMAND_LOGS.parseValue(cloudSettings.getSetting(LOG_COMMAND_LOGS.getName()))) {
                return true; // Allow command but don't log it
            }

            // Extract command name (remove / and get first word)
            String commandName = content.substring(1).split(" ")[0].toLowerCase();

            // Skip logging for sensitive commands
            if (SENSITIVE_COMMANDS.contains(commandName)) {
                logger.info("Skipped logging sensitive command: " + commandName);
                return true; // Allow command but don't log it
            }

            // Log the command asynchronously
            commandLogManager.logCommandAsync(uuid, content, server);
            
            return true;
        } catch (Exception e) {
            logger.severe("Error handling command: " + e.getMessage());
            return true; // Allow the command if there's an error
        }
    }
} 