package systems.mythical.cloudcore.config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class CloudCoreConfig {
    private static CloudCoreConfig instance;
    private final Map<String, Object> config;
    private final File configFile;
    private final Logger logger;
    private final Yaml yaml;

    private CloudCoreConfig(File dataFolder, Logger logger) {
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
        this.yaml = new Yaml();
        this.config = loadConfig();
    }

    public static CloudCoreConfig getInstance(File dataFolder, Logger logger) {
        if (instance == null) {
            instance = new CloudCoreConfig(dataFolder, logger);
        }
        return instance;
    }

    private Map<String, Object> loadConfig() {
        try {
            if (!configFile.exists()) {
                Map<String, Object> defaultConfig = createDefaultConfig();
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            try (FileInputStream input = new FileInputStream(configFile)) {
                return yaml.load(input);
            }
        } catch (Exception e) {
            logger.severe("Error loading configuration: " + e.getMessage());
            return createDefaultConfig();
        }
    }

    private void saveConfig(Map<String, Object> config) {
        try {
            yaml.dump(config, new FileWriter(configFile));
        } catch (Exception e) {
            logger.severe("Error saving configuration: " + e.getMessage());
        }
    }

    private Map<String, Object> createDefaultConfig() {
        Map<String, Object> config = new HashMap<>();
        
        // Debug mode
        config.put("debug", false);
        
        // Database settings
        Map<String, Object> database = new HashMap<>();
        database.put("host", "localhost");
        database.put("port", 3306);
        database.put("name", "mccloudadmin");
        database.put("username", "mythical");
        database.put("password", "");
        config.put("database", database);

        // Global settings
        config.put("server_name", "CloudCore");
        
        return config;
    }

    @SuppressWarnings("unchecked")
    private <T> T get(String path, T defaultValue) {
        String[] parts = path.split("\\.");
        Object current = config;

        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<String, Object>) current).get(part);
            } else {
                return defaultValue;
            }
        }

        return current != null ? (T) current : defaultValue;
    }

    public boolean isDebugMode() {
        return get("debug", false);
    }

    public String getDatabaseHost() {
        return get("database.host", "localhost");
    }

    public int getDatabasePort() {
        return get("database.port", 3306);
    }

    public String getDatabaseName() {
        return get("database.name", "cloudcore");
    }

    public String getDatabaseUsername() {
        return get("database.username", "root");
    }

    public String getDatabasePassword() {
        return get("database.password", "password");
    }
} 