package systems.mythical.cloudcore.velocity.config;

import com.velocitypowered.api.proxy.ProxyServer;
import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class VelocityConfig {
    private final Map<String, Object> config;
    private final File configFile;
    private final Logger logger;

    public VelocityConfig(File dataFolder, Logger logger) {
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
        this.config = loadConfig();
    }

    private Map<String, Object> loadConfig() {
        try {
            if (!configFile.exists()) {
                Map<String, Object> defaultConfig = createDefaultConfig();
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            try (FileInputStream input = new FileInputStream(configFile)) {
                return new Yaml().load(input);
            }
        } catch (Exception e) {
            logger.severe("Error loading configuration: " + e.getMessage());
            return createDefaultConfig();
        }
    }

    private void saveConfig(Map<String, Object> config) {
        try {
            new Yaml().dump(config, new java.io.FileWriter(configFile));
        } catch (Exception e) {
            logger.severe("Error saving configuration: " + e.getMessage());
        }
    }

    private Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Server connection settings
        Map<String, Object> server = new HashMap<>();
        server.put("host", "localhost");
        server.put("port", 9882);
        server.put("websocket_path", "/ws");
        server.put("password", "password");
        server.put("reconnect_interval", 5000);
        server.put("max_reconnect_attempts", 5);
        server.put("connection_timeout", 30000);
        config.put("server", server);

        // Plugin settings
        Map<String, Object> plugin = new HashMap<>();
        plugin.put("debug_mode", false);
        plugin.put("update_check", true);
        config.put("plugin", plugin);

        return config;
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path) {
        String[] parts = path.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return null;
            }
        }

        return (T) current;
    }

    // Server connection settings
    public String getServerHost() {
        return get("server.host");
    }

    public int getServerPort() {
        return get("server.port");
    }

    public String getWebSocketPath() {
        return get("server.websocket_path");
    }

    public int getReconnectInterval() {
        return get("server.reconnect_interval");
    }

    public int getMaxReconnectAttempts() {
        return get("server.max_reconnect_attempts");
    }

    public int getConnectionTimeout() {
        return get("server.connection_timeout");
    }

    // Plugin settings
    public boolean isDebugMode() {
        return get("plugin.debug_mode");
    }

    public boolean isUpdateCheckEnabled() {
        return get("plugin.update_check");
    }
} 