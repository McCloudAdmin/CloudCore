package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.core.CloudCore;
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

            // Initialize database tasks
            initializeDatabaseTasks();

            // Initialize commands
            initializeCommands();

            // Register events
            registerEvents();

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

    /**
     * Initializes the database tasks
     * 
     * @author MythicalSystems
     */
    public void initializeDatabaseTasks() {
        // Initialize database
        databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
        logger.info("Database connection pool initialized successfully!");

        // Initialize events
        JoinEvent.initialize(databaseManager);
        ServerSwitchEvent.initialize(databaseManager);
        QuitEvent.initialize(databaseManager);
        ChatEvent.initialize(databaseManager);
        CommandEvent.initialize(databaseManager);
        ConsoleCommand.initialize(databaseManager);
        MaintenanceSystemCommand.initialize(databaseManager, logger);
    }

    public void initializeCommands() {
        // Register proxy console command
        getProxy().getPluginManager().registerCommand(this, new ProxyConsoleCommand(this));
        // Register alert command
        getProxy().getPluginManager().registerCommand(this, new AlertCommand(this));
        // Register report command
        getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
    }


    /**
     * Registers all events for the plugin
     * 
     * @author MythicalSystems
     */
    public void registerEvents() {
        getProxy().getPluginManager().registerListener(this, new OnConnect(databaseManager, logger));
        getProxy().getPluginManager().registerListener(this, new OnServerSwitch());
        getProxy().getPluginManager().registerListener(this, new OnQuit());
        getProxy().getPluginManager().registerListener(this, new OnChat());
        getProxy().getPluginManager().registerListener(this, new OnCommand());
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