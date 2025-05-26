package systems.mythical.cloudcore.kick;

public class KickExecutorFactory {
    private static KickExecutor executor;

    public static void setExecutor(KickExecutor executor) {
        KickExecutorFactory.executor = executor;
    }

    public static KickExecutor getExecutor() {
        if (executor == null) {
            throw new IllegalStateException("KickExecutor not initialized");
        }
        return executor;
    }
} 