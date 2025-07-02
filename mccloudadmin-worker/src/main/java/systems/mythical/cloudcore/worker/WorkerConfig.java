package systems.mythical.cloudcore.worker;

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

public class WorkerConfig {
    private final Map<String, Object> config;
    private final Logger logger;

    public WorkerConfig(String configPath, Logger logger) throws IOException {
        this.logger = logger;
        
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            logger.warning("Config file not found at " + configPath + ", creating default config");
            createDefaultConfig(configFile);
        }
        
        try (InputStream input = new FileInputStream(configFile)) {
            Yaml yaml = new Yaml();
            this.config = yaml.load(input);
            logger.info("Configuration loaded from " + configPath);
        }
    }

    private void createDefaultConfig(File configFile) throws IOException {
        // Create default configuration
        Map<String, Object> defaultConfig = Map.of(
            "database", Map.of(
                "host", "localhost",
                "port", 3306,
                "database", "mccloudadmin",
                "username", "root",
                "password", "",
                "pool_size", 10
            ),
            "redis", Map.of(
                "host", "localhost",
                "port", 6379,
                "password", "",
                "pool_size", 5
            ),
            "worker", Map.of(
                "batch_size", 100,
                "poll_interval_ms", 1000,
                "max_retries", 3,
                "retry_delay_ms", 5000
            )
        );
        
        Yaml yaml = new Yaml();
        yaml.dump(defaultConfig, new java.io.FileWriter(configFile));
        logger.info("Default configuration created at " + configFile.getAbsolutePath());
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getDatabaseConfig() {
        return (Map<String, Object>) config.get("database");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getRedisConfig() {
        return (Map<String, Object>) config.get("redis");
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> getWorkerConfig() {
        return (Map<String, Object>) config.get("worker");
    }

    public String getDatabaseHost() {
        return (String) getDatabaseConfig().get("host");
    }

    public int getDatabasePort() {
        return (Integer) getDatabaseConfig().get("port");
    }

    public String getDatabaseName() {
        return (String) getDatabaseConfig().get("database");
    }

    public String getDatabaseUsername() {
        return (String) getDatabaseConfig().get("username");
    }

    public String getDatabasePassword() {
        return (String) getDatabaseConfig().get("password");
    }

    public int getDatabasePoolSize() {
        return (Integer) getDatabaseConfig().get("pool_size");
    }

    public String getRedisHost() {
        return (String) getRedisConfig().get("host");
    }

    public int getRedisPort() {
        return (Integer) getRedisConfig().get("port");
    }

    public String getRedisPassword() {
        return (String) getRedisConfig().get("password");
    }

    public int getRedisPoolSize() {
        return (Integer) getRedisConfig().get("pool_size");
    }

    public int getBatchSize() {
        return (Integer) getWorkerConfig().get("batch_size");
    }

    public long getPollIntervalMs() {
        return (Long) getWorkerConfig().get("poll_interval_ms");
    }

    public int getMaxRetries() {
        return (Integer) getWorkerConfig().get("max_retries");
    }

    public long getRetryDelayMs() {
        return (Long) getWorkerConfig().get("retry_delay_ms");
    }
} 