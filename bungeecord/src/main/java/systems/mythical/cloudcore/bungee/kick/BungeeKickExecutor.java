package systems.mythical.cloudcore.bungee.kick;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import systems.mythical.cloudcore.kick.KickExecutor;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;
import systems.mythical.cloudcore.utils.CloudLogger;

import java.util.UUID;

public class BungeeKickExecutor implements KickExecutor {
    private static BungeeKickExecutor instance;
    private static final CloudLogger logger = CloudLoggerFactory.get();

    private BungeeKickExecutor() {}

    public static BungeeKickExecutor getInstance() {
        if (instance == null) {
            instance = new BungeeKickExecutor();
        }
        return instance;
    }

    @SuppressWarnings("deprecation")
	@Override
    public void executeKick(UUID uuid, String reason) {
        var player = ProxyServer.getInstance().getPlayer(uuid);
        if (player != null) {
            // Execute kick on the proxy thread to avoid any potential threading issues
            ProxyServer.getInstance().getScheduler().runAsync(
                ProxyServer.getInstance().getPluginManager().getPlugin("CloudCore"),
                () -> {
                    try {
                        player.disconnect(TextComponent.fromLegacyText(reason));
                        logger.info("Successfully kicked player " + player.getName() + " (" + uuid + "): " + reason);
                    } catch (Exception e) {
                        logger.error("Failed to kick player " + player.getName() + " (" + uuid + "): " + e.getMessage());
                    }
                }
            );
        }
    }
} 