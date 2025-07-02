package systems.mythical.cloudcore.config;

import org.yaml.snakeyaml.Yaml;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

public class CloudCoreConfig {
    private static CloudCoreConfig instance;
    private final Map<String, Object> config;
    private final File configFile;
    private final Logger logger;
    private final Yaml yaml;
    private final boolean isSpigot;

    private CloudCoreConfig(File dataFolder, Logger logger, boolean isSpigot) {
        this.logger = logger;
        this.configFile = new File(dataFolder, "config.yml");
        this.yaml = new Yaml();
        this.config = loadConfig(isSpigot);
        this.isSpigot = isSpigot;
        logger.info("CloudCoreConfig instance created for " + (isSpigot ? "Spigot" : "Proxy") + "!");
    }

    public static CloudCoreConfig getInstance(File dataFolder, Logger logger, boolean isSpigot) {
        logger.info("Getting CloudCoreConfig instance for " + (isSpigot ? "Spigot" : "Proxy") + "!");
        if (instance == null) {
            instance = new CloudCoreConfig(dataFolder, logger, isSpigot);
        }
        return instance;
    }

    private Map<String, Object> loadConfig(boolean isSpigot) {
        try {
            if (!configFile.exists()) {
                Map<String, Object> defaultConfig = createDefaultConfig(isSpigot);
                saveConfig(defaultConfig);
                return defaultConfig;
            }

            try (FileInputStream input = new FileInputStream(configFile)) {
                return yaml.load(input);
            }
        } catch (Exception e) {
            logger.severe("Error loading configuration: " + e.getMessage());
            return createDefaultConfig(isSpigot );
        }
    }

    private void saveConfig(Map<String, Object> config) {
        try {
            yaml.dump(config, new FileWriter(configFile));
        } catch (Exception e) {
            logger.severe("Error saving configuration: " + e.getMessage());
        }
    }

    private Map<String, Object> createDefaultConfig(boolean isSpigot) {
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


        Map<String, Object> worker = new HashMap<>();
        if (isSpigot) {
            worker.put("name", "server");
            logger.info("[SPIGOT] Worker name set to server");
        } else {
            worker.put("name", "proxy");
            logger.info("[PROXY] Worker name set to proxy");
        }
        worker.put("key", generateRandomKey());
        worker.put("uuid", UUID.randomUUID().toString());
        config.put("worker", worker);
        
        return config;
    }

    private String generateRandomKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
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

    public String getWorkerName() {
        if (isSpigot) {
            return get("worker.name", "server");
        } else {
            return get("worker.name", "proxy");
        }
    }

    public String getWorkerKey() {
        return get("worker.key", "");
    }

    public String getWorkerUUID() {
        return get("worker.uuid", "");
    }
} 