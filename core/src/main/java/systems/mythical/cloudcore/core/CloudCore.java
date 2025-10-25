package systems.mythical.cloudcore.core;

import systems.mythical.cloudcore.config.CloudCoreConfig;
import java.io.File;
import java.util.logging.Logger;

public class CloudCore {
    private final Logger logger;
    private final File dataFolder;
    private final CloudCoreConfig config;
    private final boolean isSpigot;

    public CloudCore(File dataFolder, Logger logger, boolean isSpigot) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        this.isSpigot = isSpigot;
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        // Initialize configuration
        this.config = CloudCoreConfig.getInstance(dataFolder, logger, isSpigot);
        logger.info("CloudCore has been initialized for " + (isSpigot ? "Spigot" : "Proxy") + "!");
        if (config.isDebugMode()) {
            logger.info("Debug mode is enabled");
        } else {
            logger.info("Forcing plugin to run in production mode.");
        }
    }
    
    public void shutdown() {
        logger.info("Shutting down CloudCore ... (This may take a few seconds) on platform: "+(isSpigot?"Spigot":"Proxy"));
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