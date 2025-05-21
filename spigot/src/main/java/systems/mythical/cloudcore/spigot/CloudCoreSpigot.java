package systems.mythical.cloudcore.spigot;

import org.bukkit.plugin.java.JavaPlugin;
import systems.mythical.cloudcore.spigot.config.SpigotConfig;
import systems.mythical.cloudcore.spigot.websocket.SpigotWebSocketClient;

public class CloudCoreSpigot extends JavaPlugin {
    private SpigotConfig config;
    private SpigotWebSocketClient webSocketClient;

    @Override
    public void onEnable() {
        // Create data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize configuration
        config = new SpigotConfig(this);

        // Initialize WebSocket client
        webSocketClient = new SpigotWebSocketClient(this, getServer());
        webSocketClient.connect();

        getLogger().info("CloudCore Spigot plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
        getLogger().info("CloudCore Spigot plugin has been disabled!");
    }

    public SpigotConfig getPluginConfig() {
        return config;
    }
} 