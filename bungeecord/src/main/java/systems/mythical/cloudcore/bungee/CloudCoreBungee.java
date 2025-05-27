package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.events.ChatEvent;
import systems.mythical.cloudcore.kick.KickExecutorFactory;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.events.QuitEvent;
import systems.mythical.cloudcore.bungee.events.OnConnect;
import systems.mythical.cloudcore.bungee.events.OnServerSwitch;
import systems.mythical.cloudcore.bungee.commands.ProxyConsoleCommand;
import systems.mythical.cloudcore.bungee.events.OnChat;
import systems.mythical.cloudcore.bungee.kick.BungeeKickExecutor;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.bungee.events.OnQuit;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.permissions.PlatformPermissionBridge;
import systems.mythical.cloudcore.events.CommandEvent;
import systems.mythical.cloudcore.bungee.events.OnCommand;
import systems.mythical.cloudcore.bungee.commands.AlertCommand;
import systems.mythical.cloudcore.bungee.commands.ReportCommand;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.bungee.commands.CloudCoreCommand;

import java.util.UUID;
import java.util.logging.Logger;

public class CloudCoreBungee extends Plugin {
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;
    private final Logger logger = getLogger();

    @SuppressWarnings("deprecation")
    @Override
    public void onEnable() {
        logger.info("CloudCore has been initialized!");
        logger.info("Forcing plugin to run in production mode.");

        if (getProxy().getPluginManager().getPlugin("packetevents") == null) {
            logger.info("PacketEvents is not installed, disabling CloudCore BungeeCord plugin.");
            try {
                logger.info("Downloading PacketEvents...");
                java.nio.file.Path pluginFolder = getDataFolder().toPath().getParent();
                java.net.URL url = new java.net.URL(
                        "https://github.com/retrooper/packetevents/releases/download/v2.8.0/packetevents-bungeecord-2.8.0.jar");
                java.nio.file.Files.copy(url.openStream(), pluginFolder.resolve("packetevents-bungeecord-2.8.0.jar"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully downloaded PacketEvents to plugins folder");
            } catch (Exception e) {
                logger.severe("Failed to download PacketEvents: " + e.getMessage());
                e.printStackTrace();
            }
            getProxy().stop();
            return;
        }

        KickExecutorFactory.setExecutor(BungeeKickExecutor.getInstance());

        // Set up permission checker
        PlatformPermissionBridge.setPermissionChecker((UUID uuid, String permission) -> {
            var player = getProxy().getPlayer(uuid);
            return player != null && player.hasPermission(permission);
        });

        try {
            // Initialize CloudCore
            cloudCore = new CloudCore(getDataFolder(), logger);

            // Initialize database
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
            logger.info("Database connection pool initialized successfully!");

            // Initialize settings
            CloudSettings cloudSettings = CloudSettings.getInstance(databaseManager, logger);
            SettingsManager settingsManager = SettingsManager.getInstance(cloudSettings, logger);

            // Initialize events based on settings
            CommonSettings.BooleanSetting logChatEvents = new CommonSettings.BooleanSetting(Settings.LOG_CHAT, true);
            CommonSettings.BooleanSetting logCommandEvents = new CommonSettings.BooleanSetting(Settings.LOG_COMMANDS,
                    true);

            JoinEvent.initialize(databaseManager);
            logger.info("[CloudCore] Join events initialized");

            ServerSwitchEvent.initialize(databaseManager);
            logger.info("[CloudCore] Server switch events initialized");

            QuitEvent.initialize(databaseManager);
            logger.info("[CloudCore] Quit events initialized");

            if (settingsManager.getValue(logChatEvents)) {
                ChatEvent.initialize(databaseManager);
                logger.info("[CloudCore] Chat events initialized");
            }

            if (settingsManager.getValue(logCommandEvents)) {
                CommandEvent.initialize(databaseManager);
                logger.info("[CloudCore] Command events initialized");
                ConsoleCommand.initialize(databaseManager);
                logger.info("[CloudCore] Console commands initialized");
            }

            MaintenanceSystemCommand.initialize(databaseManager, logger);

            // Register events only if they are enabled
            getProxy().getPluginManager().registerListener(this, new OnConnect(databaseManager, logger));
            getProxy().getPluginManager().registerListener(this, new OnServerSwitch());
            getProxy().getPluginManager().registerListener(this, new OnQuit());
            if (settingsManager.getValue(logChatEvents)) {
                getProxy().getPluginManager().registerListener(this, new OnChat());
            }
            if (settingsManager.getValue(logCommandEvents)) {
                getProxy().getPluginManager().registerListener(this, new OnCommand());
            }

            // Initialize commands
            initializeCommands(settingsManager);

            logger.info("CloudCore BungeeCord plugin has been enabled!");
        } catch (Exception e) {
            logger.severe("Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Mark all users as offline before shutdown
        if (databaseManager != null) {
            UserManager userManager = UserManager.getInstance(databaseManager, logger);
            userManager.markAllUsersOffline();
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }
        logger.info("CloudCore BungeeCord plugin has been disabled!");
    }

    public void initializeCommands(SettingsManager settingsManager) {
        // Register CloudCore command
        getProxy().getPluginManager().registerCommand(this, new CloudCoreCommand(this));
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_ALERT_COMMAND, false))) {
            // Register alert command
            getProxy().getPluginManager().registerCommand(this, new AlertCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.REPORT_SYSTEM_ENABLED, false))) {
            // Register report command
            getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_CONSOLE_COMMAND, false))) {
            // Register proxy console command
            getProxy().getPluginManager().registerCommand(this, new ProxyConsoleCommand(this));
        }
    }

    /**
     * Gets the database manager
     * 
     * @author MythicalSystems
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}