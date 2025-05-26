package systems.mythical.cloudcore.bungee.events;

import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import systems.mythical.cloudcore.events.QuitEvent;

public class OnQuit implements Listener {
    @EventHandler
    public void onQuit(PlayerDisconnectEvent event) {
        QuitEvent.onPlayerQuit(event.getPlayer().getName(), event.getPlayer().getUniqueId());
    }
} 