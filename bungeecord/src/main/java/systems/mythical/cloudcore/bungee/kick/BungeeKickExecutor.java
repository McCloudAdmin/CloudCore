package systems.mythical.cloudcore.bungee.kick;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import systems.mythical.cloudcore.kick.KickExecutor;

import java.util.UUID;
import java.util.logging.Logger;

public class BungeeKickExecutor implements KickExecutor {
    private static BungeeKickExecutor instance;
    private static final Logger logger = Logger.getLogger(BungeeKickExecutor.class.getName());

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
                        logger.severe("Failed to kick player " + player.getName() + " (" + uuid + "): " + e.getMessage());
                    }
                }
            );
        }
    }
} 