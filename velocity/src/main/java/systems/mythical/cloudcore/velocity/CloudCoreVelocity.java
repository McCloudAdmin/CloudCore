package systems.mythical.cloudcore.velocity;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import systems.mythical.cloudcore.velocity.config.VelocityConfig;
import systems.mythical.cloudcore.velocity.websocket.VelocityWebSocketClient;

import java.io.File;
import java.util.logging.Logger;

@Plugin(
    id = "cloudcore",
    name = "CloudCore",
    version = "1.0.0",
    description = "CloudCore for Velocity",
    authors = {"MythicalSystems"}
)
public class CloudCoreVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private VelocityConfig config;
    private VelocityWebSocketClient webSocketClient;

    public CloudCoreVelocity(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(ProxyInitializeEvent event) {
        // Initialize configuration
        File dataFolder = new File("plugins/CloudCore");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        config = new VelocityConfig(dataFolder, logger);

        // Initialize WebSocket client
        webSocketClient = new VelocityWebSocketClient(this, server);
        webSocketClient.connect();
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        if (webSocketClient != null) {
            webSocketClient.disconnect();
        }
    }

    public VelocityConfig getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }
}