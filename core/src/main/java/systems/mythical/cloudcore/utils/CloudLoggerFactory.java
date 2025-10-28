package systems.mythical.cloudcore.utils;

public final class CloudLoggerFactory {
    private static volatile CloudLogger INSTANCE;

    private CloudLoggerFactory() {}

    public static void init(CloudLogger logger) {
        INSTANCE = logger;
    }

    public static CloudLogger get() {
        if (INSTANCE == null) {
            throw new IllegalStateException("CloudLoggerFactory not initialized");
        }
        return INSTANCE;
    }
}


