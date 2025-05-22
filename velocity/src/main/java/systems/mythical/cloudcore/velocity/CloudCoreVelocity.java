package systems.mythical.cloudcore.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.velocity.events.OnConnect;
import systems.mythical.cloudcore.velocity.events.OnServerSwitch;

import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(
    id = "cloudcore",
    name = "CloudCore",
    version = "1.0-SNAPSHOT",
    description = "Adds cooldown to server switching",
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

        try {
            // Initialize CloudCore
            cloudCore = new CloudCore(dataFolder.toFile(), logger);
            
            // Initialize database connection
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
            
            // Initialize JoinEvent
            JoinEvent.initialize(databaseManager);
            ServerSwitchEvent.initialize(databaseManager);
            // Register events
            server.getEventManager().register(this, new OnConnect());
            server.getEventManager().register(this, new OnServerSwitch());

            logger.info("CloudCore Velocity plugin has been enabled!");
        } catch (Exception e) {
            logger.severe("Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }
        logger.info("CloudCore Velocity plugin has been disabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }
}