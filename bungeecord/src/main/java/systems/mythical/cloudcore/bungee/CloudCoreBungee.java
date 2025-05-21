package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.ProxyServer;
import systems.mythical.cloudcore.bungee.config.BungeeConfig;
import systems.mythical.cloudcore.bungee.websocket.BungeeWebSocketClient;

import java.io.File;

public class CloudCoreBungee extends Plugin {
    private BungeeConfig config;
    private BungeeWebSocketClient webSocketClient;

    @Override
    public void onEnable() {
        // Initialize configuration
        File dataFolder = getDataFolder();
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        config = new BungeeConfig(dataFolder, getLogger());

        // Initialize WebSocket client
        webSocketClient = new BungeeWebSocketClient(this, getProxy());
        webSocketClient.connect();
    }

    @Override
    public void onDisable() {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

    public BungeeConfig getConfig() {
        return config;
    }
} 