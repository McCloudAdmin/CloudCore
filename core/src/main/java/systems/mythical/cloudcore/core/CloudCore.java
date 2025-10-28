package systems.mythical.cloudcore.core;

import systems.mythical.cloudcore.config.CloudCoreConfig;
import java.io.File;
import java.util.logging.Logger;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

public class CloudCore {
    private final Logger logger;
    private final File dataFolder;
    private final CloudCoreConfig config;
    private final boolean isSpigot;
    private final CloudLogger cloudLogger;

    public CloudCore(File dataFolder, Logger logger, boolean isSpigot) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.isSpigot = isSpigot;
        this.cloudLogger = CloudLoggerFactory.get();
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Initialize configuration
        this.config = CloudCoreConfig.getInstance(dataFolder, logger, isSpigot);
        cloudLogger.info("CloudCore has been initialized for " + (isSpigot ? "Spigot" : "Proxy") + "!");
        if (config.isDebugMode()) {
            cloudLogger.info("Debug mode is enabled");
        } else {
            cloudLogger.info("Forcing plugin to run in production mode.");
        }
    }
    
    public void shutdown() {
        cloudLogger.info("Shutting down CloudCore ... (This may take a few seconds) on platform: "+(isSpigot?"Spigot":"Proxy"));
    }

    public CloudCoreConfig getConfig() {
        return config;
    }

    public Logger getLogger() {
        return logger;
    }

    public File getDataFolder() {
        return dataFolder;
    }
} 