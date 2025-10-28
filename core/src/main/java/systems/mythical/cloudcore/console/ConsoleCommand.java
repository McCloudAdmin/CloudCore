package systems.mythical.cloudcore.console;

import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.messages.MessageManager;

import java.util.UUID;
import java.util.logging.Logger;

public class ConsoleCommand {
    private static ConsoleExecutorManager consoleExecutorManager;
    private static MessageManager messageManager;

    public static void initialize(DatabaseManager databaseManager, Logger logger) {
        consoleExecutorManager = ConsoleExecutorManager.getInstance(databaseManager, logger);
        messageManager = MessageManager.getInstance(databaseManager, logger);
    }

    public static boolean onConsoleCommand(String displayName, String command) {
        if (consoleExecutorManager == null || messageManager == null) {
            return false; // Block command if not initialized
        }

        try {
            // Check if player is a console executor
            if (!consoleExecutorManager.isConsoleExecutor(displayName)) {
                return false;
            }            
            return true;
        } catch (Exception e) {
            return false; // Block command if there's an error
        }   
    }

    /**
     * Handles a console command execution request
     *
     * @param uuid The UUID of the player requesting the command execution
     * @param command The command to execute
     * @return true if the command should be executed, false if it should be blocked
     */
    public static boolean onConsoleCommand(UUID uuid, String command) {
        if (consoleExecutorManager == null || messageManager == null) {
            return false; // Block command if not initialized
        }

        try {
            // Check if player is a console executor
            if (!consoleExecutorManager.isConsoleExecutor(uuid)) {
                return false;
            }            
            return true;
        } catch (Exception e) {
            return false; // Block command if there's an error
        }
    }
} 