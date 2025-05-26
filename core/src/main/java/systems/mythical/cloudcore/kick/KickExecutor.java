package systems.mythical.cloudcore.kick;

import java.util.UUID;

public interface KickExecutor {
    void executeKick(UUID uuid, String reason);
} 