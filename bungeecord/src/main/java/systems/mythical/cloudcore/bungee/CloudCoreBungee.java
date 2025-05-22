package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.bungee.events.OnConnect;
import systems.mythical.cloudcore.bungee.events.OnServerSwitch;

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
                java.net.URL url = new java.net.URL("https://github.com/retrooper/packetevents/releases/download/v2.8.0/packetevents-bungeecord-2.8.0.jar");
                java.nio.file.Files.copy(url.openStream(), pluginFolder.resolve("packetevents-bungeecord-2.8.0.jar"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                logger.info("Successfully downloaded PacketEvents to plugins folder");
            } catch (Exception e) {
                logger.severe("Failed to download PacketEvents: " + e.getMessage());
                e.printStackTrace();
            }
            getProxy().stop();
            return;
        }

        try {
            // Initialize CloudCore
            cloudCore = new CloudCore(getDataFolder(), logger);
            
            // Initialize database
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
            logger.info("Database connection pool initialized successfully!");

            // Initialize JoinEvent
            JoinEvent.initialize(databaseManager);
            ServerSwitchEvent.initialize(databaseManager);

            // Register events
            getProxy().getPluginManager().registerListener(this, new OnConnect());
            getProxy().getPluginManager().registerListener(this, new OnServerSwitch());

            logger.info("CloudCore BungeeCord plugin has been enabled!");
        } catch (Exception e) {
            logger.severe("Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }
        logger.info("CloudCore BungeeCord plugin has been disabled!");
    }
} 