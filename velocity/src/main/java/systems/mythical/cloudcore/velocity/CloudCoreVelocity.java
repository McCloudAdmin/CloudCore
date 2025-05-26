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
import java.util.List;

@Plugin(
    id = "cloudcore",
    name = "CloudCore",
    version = "1.0-SNAPSHOT",
    description = "The plugin to run McCloudAdminPanel",
    authors = {"MythicalSystems"}
)
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
                java.net.URL url = new java.net.URL("https://github.com/retrooper/packetevents/releases/download/v2.8.0/packetevents-velocity-2.8.0.jar");
                java.nio.file.Files.copy(url.openStream(), pluginFolder.resolve("packetevents-velocity-2.8.0.jar"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
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
            
            // Initialize events
            JoinEvent.initialize(databaseManager);
            ServerSwitchEvent.initialize(databaseManager);
            QuitEvent.initialize(databaseManager);
            ChatEvent.initialize(databaseManager);
            CommandEvent.initialize(databaseManager);
            ConsoleCommand.initialize(databaseManager);
            MaintenanceSystemCommand.initialize(databaseManager, logger);

            // Register events
            server.getEventManager().register(this, new OnConnect(databaseManager, logger));
            server.getEventManager().register(this, new OnServerSwitch());
            server.getEventManager().register(this, new OnQuit());
            server.getEventManager().register(this, new OnChat());
            server.getEventManager().register(this, new OnCommand());

            logger.info("[CloudCore] Initializing commands...");
            initializeCommands();

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

    public void initializeCommands() {
        logger.info("[CloudCore] Registering commands...");
        CommandManager commandManager = server.getCommandManager();

        // Register /proxyconsole and aliases
        try {
            SimpleCommand proxyConsole = new ProxyConsoleCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("proxyconsole").plugin(this).build();
            commandManager.register(mainMeta, proxyConsole);
            logger.info("[CloudCore] /proxyconsole command registered successfully.");
            for (String alias : List.of("pcex")) {
                CommandMeta aliasMeta = commandManager.metaBuilder(alias).plugin(this).build();
                commandManager.register(aliasMeta, proxyConsole);
                logger.info("[CloudCore] /" + alias + " alias for proxyconsole registered.");
            }
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /proxyconsole: " + e.getMessage());
            e.printStackTrace();
        }

        // Register /alert (add aliases if needed)
        try {
            SimpleCommand alert = new AlertCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("alert").plugin(this).build();
            commandManager.register(mainMeta, alert);
            logger.info("[CloudCore] /alert command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /alert: " + e.getMessage());
            e.printStackTrace();
        }

        // Register /report (add aliases if needed)
        try {
            SimpleCommand report = new ReportCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("report").plugin(this).build();
            commandManager.register(mainMeta, report);
            logger.info("[CloudCore] /report command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /report: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}