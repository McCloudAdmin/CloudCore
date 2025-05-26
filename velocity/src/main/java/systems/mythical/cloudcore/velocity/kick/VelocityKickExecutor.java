package systems.mythical.cloudcore.velocity.kick;

import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.kick.KickExecutor;

import java.util.UUID;
import java.util.logging.Logger;

public class VelocityKickExecutor implements KickExecutor {
    private static VelocityKickExecutor instance;
    private final ProxyServer proxy;
    private static final Logger logger = Logger.getLogger(VelocityKickExecutor.class.getName());

    private VelocityKickExecutor(ProxyServer proxy) {
        this.proxy = proxy;
    }

    public static VelocityKickExecutor getInstance(ProxyServer proxy) {
        if (instance == null) {
            instance = new VelocityKickExecutor(proxy);
        }
        return instance;
    }

    @Override
    public void executeKick(UUID uuid, String reason) {
        proxy.getPlayer(uuid).ifPresent(player -> {
            try {
                player.disconnect(LegacyComponentSerializer.legacyAmpersand().deserialize(reason));
                logger.info("Successfully kicked player " + player.getUsername() + " (" + uuid + "): " + reason);
            } catch (Exception e) {
                logger.severe("Failed to kick player " + player.getUsername() + " (" + uuid + "): " + e.getMessage());
            }
        });
    }
} 