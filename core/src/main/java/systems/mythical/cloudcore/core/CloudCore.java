package systems.mythical.cloudcore.core;

import java.io.File;
import java.util.logging.Logger;

public class CloudCore {
    private final Logger logger;
    private final File dataFolder;

    public CloudCore(File dataFolder, Logger logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
        
        // Create data folder if it doesn't exist
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
    }
    
    public void shutdown() {

    }

} 