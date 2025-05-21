package systems.mythical.mccloudadmin.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ServerConfig {
    private static final Logger logger = LoggerFactory.getLogger(ServerConfig.class);
    private static ServerConfig instance;
    private final Map<String, Object> config;

    private ServerConfig() {
        this.config = loadConfig();
    }

    public static synchronized ServerConfig getInstance() {
        if (instance == null) {
            instance = new ServerConfig();
        }
        return instance;
    }

    private Map<String, Object> loadConfig() {
        try {
            // First try to load from the current directory
            Path configPath = Paths.get("config.yml");
            if (!Files.exists(configPath)) {
                // If not found, try to load from resources
                InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml");
                if (input == null) {
                    logger.warn("No config.yml found, using default configuration");
                    return createDefaultConfig();
                }
                return new Yaml().load(input);
            }

            try (FileInputStream input = new FileInputStream(configPath.toFile())) {
                return new Yaml().load(input);
            }
        } catch (Exception e) {
            logger.error("Error loading configuration", e);
            return createDefaultConfig();
        }
    }

    private Map<String, Object> createDefaultConfig() {
        return Map.of(
            "server", Map.of(
                "port", 8080,
                "host", "0.0.0.0"
            ),
            "websocket", Map.of(
                "path", "/ws",
                "maxFrameSize", 65536
            ),
            "auth", Map.of(
                "jwtSecret", "your-256-bit-secret-key-here-make-it-long-and-secure",
                "tokenExpiration", 86400
            ),
            "logging", Map.of(
                "level", "INFO",
                "file", "logs/mccloudadmin-server.log"
            ),
            "database", Map.of(
                "url", "jdbc:mysql://localhost:3306/mccloudadmin",
                "username", "mythical",
                "password", "",
                "maxPoolSize", 10,
                "minIdle", 5,
                "idleTimeout", 300000,
                "connectionTimeout", 30000
            )
        );
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

    public int getPort() {
        return get("server.port");
    }

    public String getHost() {
        return get("server.host");
    }

    public String getWebSocketPath() {
        return get("websocket.path");
    }

    public int getMaxFrameSize() {
        return get("websocket.maxFrameSize");
    }

    public String getJwtSecret() {
        return get("auth.jwtSecret");
    }

    public int getTokenExpiration() {
        return get("auth.tokenExpiration");
    }

    public String getLogLevel() {
        return get("logging.level");
    }

    public String getLogFile() {
        return get("logging.file");
    }

    // Database configuration getters
    public String getDatabaseUrl() {
        return get("database.url");
    }

    public String getDatabaseUsername() {
        return get("database.username");
    }

    public String getDatabasePassword() {
        return get("database.password");
    }

    public int getDatabaseMaxPoolSize() {
        return get("database.maxPoolSize");
    }

    public int getDatabaseMinIdle() {
        return get("database.minIdle");
    }

    public int getDatabaseIdleTimeout() {
        return get("database.idleTimeout");
    }

    public int getDatabaseConnectionTimeout() {
        return get("database.connectionTimeout");
    }
} 