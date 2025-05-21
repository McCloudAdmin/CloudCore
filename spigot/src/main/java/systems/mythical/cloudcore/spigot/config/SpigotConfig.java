package systems.mythical.cloudcore.spigot.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;

import java.io.File;
import java.io.IOException;

public class SpigotConfig {
    private final CloudCoreSpigot plugin;
    private final File configFile;
    private FileConfiguration config;

    public SpigotConfig(CloudCoreSpigot plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.getDataFolder().mkdirs();
            createDefaultConfig();
        }

        try {
            config = YamlConfiguration.loadConfiguration(configFile);
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load config.yml: " + e.getMessage());
            createDefaultConfig();
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save config.yml: " + e.getMessage());
        }
    }

    private void createDefaultConfig() {
        config = new YamlConfiguration();
        
        // Server connection settings
        config.set("server.host", "localhost");
        config.set("server.port", 9882);
        config.set("server.websocket_path", "/ws");
        config.set("server.password", "password");
        config.set("server.reconnect_interval", 5000);
        config.set("server.max_reconnect_attempts", 5);
        config.set("server.connection_timeout", 30000);

        // Plugin settings
        config.set("plugin.debug", false);
        config.set("plugin.update_check", true);

        saveConfig();
    }

    public Object get(String path) {
        return config.get(path);
    }

    public String getServerHost() {
        return config.getString("server.host", "localhost");
    }

    public int getServerPort() {
        return config.getInt("server.port", 9882);
    }

    public String getWebSocketPath() {
        return config.getString("server.websocket_path", "/ws");
    }

    public int getReconnectInterval() {
        return config.getInt("server.reconnect_interval", 5000);
    }

    public int getMaxReconnectAttempts() {
        return config.getInt("server.max_reconnect_attempts", 5);
    }

    public int getConnectionTimeout() {
        return config.getInt("server.connection_timeout", 30000);
    }

    public boolean isDebugMode() {
        return config.getBoolean("plugin.debug", false);
    }

    public boolean isUpdateCheckEnabled() {
        return config.getBoolean("plugin.update_check", true);
    }
} 