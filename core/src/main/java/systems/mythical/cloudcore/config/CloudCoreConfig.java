package systems.mythical.cloudcore.config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CloudCoreConfig {
    private static CloudCoreConfig instance;
    private final Map<String, Object> config;
    private final File configFile;
    private final Logger logger;

    private CloudCoreConfig(File dataFolder, Logger logger) {
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
        this.config = loadConfig();
    }

    public static synchronized CloudCoreConfig getInstance(File dataFolder, Logger logger) {
        if (instance == null) {
            instance = new CloudCoreConfig(dataFolder, logger);
        }
        return instance;
    }

    private Map<String, Object> loadConfig() {
        try {
            if (!configFile.exists()) {
                // Create default config
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

    // Plugin settings
    public boolean isDebugMode() {
        return get("plugin.debug_mode");
    }

    public boolean isUpdateCheckEnabled() {
        return get("plugin.update_check");
    }
} 