package systems.mythical.cloudcore.velocity;

/**
 * Google Imports
 */
import com.google.inject.Inject;
/**
 * CloudCore Imports
 */
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.QuitEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.events.ChatEvent;
import systems.mythical.cloudcore.events.CommandEvent;
import systems.mythical.cloudcore.kick.KickExecutorFactory;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.velocity.events.OnConnect;
import systems.mythical.cloudcore.velocity.events.OnQuit;
import systems.mythical.cloudcore.velocity.events.OnServerSwitch;
import systems.mythical.cloudcore.velocity.events.OnChat;
import systems.mythical.cloudcore.velocity.events.OnCommand;
import systems.mythical.cloudcore.velocity.kick.VelocityKickExecutor;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.permissions.PlatformPermissionBridge;
import systems.mythical.cloudcore.velocity.commands.ProxyConsoleCommand;
import systems.mythical.cloudcore.velocity.commands.AlertCommand;
import systems.mythical.cloudcore.velocity.commands.ReportCommand;
import systems.mythical.cloudcore.velocity.commands.CloudCoreCommand;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
/**
 * Velocity Imports
 */
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandManager;

/*
 * Java Imports
 */
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

@Plugin(id = "cloudcore", name = "CloudCore", version = "1.0-SNAPSHOT", description = "The plugin to run McCloudAdminPanel", authors = {
        "MythicalSystems" })
public class CloudCoreVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataFolder;
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;

    @Inject
    public CloudCoreVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("[CloudCore] onProxyInitialization called.");
        logger.info("CloudCore has been initialized!");
        logger.info("Forcing plugin to run in production mode.");

        if (!server.getPluginManager().getPlugin("packetevents").isPresent()) {
            logger.info("PacketEvents is not installed, disabling CloudCore Velocity plugin.");
            try {
                logger.info("Downloading PacketEvents...");
                java.nio.file.Path pluginFolder = dataFolder.getParent();
                java.net.URL url = new java.net.URL(
                        "https://github.com/retrooper/packetevents/releases/download/v2.8.0/packetevents-velocity-2.8.0.jar");
                java.nio.file.Files.copy(url.openStream(), pluginFolder.resolve("packetevents-velocity-2.8.0.jar"),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully downloaded PacketEvents to plugins folder");
            } catch (Exception e) {
                logger.severe("Failed to download PacketEvents: " + e.getMessage());
                e.printStackTrace();
            }
            server.shutdown();
            return;
        }

        KickExecutorFactory.setExecutor(VelocityKickExecutor.getInstance(server));

        // Set up permission checker
        PlatformPermissionBridge.setPermissionChecker((UUID uuid, String permission) -> {
            var player = server.getPlayer(uuid);
            return player.isPresent() && player.get().hasPermission(permission);
        });

        try {
            logger.info("[CloudCore] Initializing CloudCore...");
            cloudCore = new CloudCore(dataFolder.toFile(), logger);

            logger.info("[CloudCore] Initializing database...");
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);

            // Initialize settings
            CloudSettings cloudSettings = CloudSettings.getInstance(databaseManager, logger);
            SettingsManager settingsManager = SettingsManager.getInstance(cloudSettings, logger);

            // Initialize events based on settings
            CommonSettings.BooleanSetting logChatEvents = new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false);
            CommonSettings.BooleanSetting logCommandEvents = new CommonSettings.BooleanSetting(Settings.LOG_COMMANDS,
                    false);

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

            // Register events
            server.getEventManager().register(this, new OnConnect(databaseManager, logger));
            server.getEventManager().register(this, new OnServerSwitch());
            server.getEventManager().register(this, new OnQuit());

            if (settingsManager.getValue(logChatEvents)) {
                server.getEventManager().register(this, new OnChat());
            }
            if (settingsManager.getValue(logCommandEvents)) {
                server.getEventManager().register(this, new OnCommand());
            }

            logger.info("[CloudCore] Initializing commands...");
            initializeCommands(cloudSettings, settingsManager);

            logger.info("[CloudCore] CloudCore Velocity plugin has been enabled!");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

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
        logger.info("CloudCore Velocity plugin has been disabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onDisable();
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public void initializeCommands(CloudSettings cloudSettings, SettingsManager settingsManager) {
        logger.info("[CloudCore] Registering commands...");
        CommandManager commandManager = server.getCommandManager();

        // Register CloudCore command
        try {
            SimpleCommand cloudCore = new CloudCoreCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("cloudcore")
                    .aliases("cc")
                    .plugin(this)
                    .build();
            commandManager.register(mainMeta, cloudCore);
            logger.info("[CloudCore] /cloudcore command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /cloudcore: " + e.getMessage());
            e.printStackTrace();
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_CONSOLE_COMMAND, false))) {
            try {
                SimpleCommand proxyConsole = new ProxyConsoleCommand(this);
                CommandMeta mainMeta = commandManager.metaBuilder("proxyconsole")
                        .aliases("pcex")
                        .plugin(this)
                        .build();
                commandManager.register(mainMeta, proxyConsole);
                logger.info("[CloudCore] /proxyconsole command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /proxyconsole: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_ALERT_COMMAND, false))) {
            try {
                SimpleCommand alert = new AlertCommand(this);
                CommandMeta mainMeta = commandManager.metaBuilder("alert")
                        .plugin(this)
                        .build();
                commandManager.register(mainMeta, alert);
                logger.info("[CloudCore] /alert command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /alert: " + e.getMessage());
                e.printStackTrace();
            }
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.REPORT_SYSTEM_ENABLED, false))) {
            try {
                SimpleCommand report = new ReportCommand(this);
                CommandMeta mainMeta = commandManager.metaBuilder("report")
                        .plugin(this)
                        .build();
                commandManager.register(mainMeta, report);
                logger.info("[CloudCore] /report command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /report: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}