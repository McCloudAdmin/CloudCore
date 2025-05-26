package systems.mythical.cloudcore.velocity.events;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import systems.mythical.cloudcore.events.QuitEvent;

public class OnQuit {
    @Subscribe
    public void onQuit(DisconnectEvent event) {
        QuitEvent.onPlayerQuit(event.getPlayer().getUsername(), event.getPlayer().getUniqueId());
    }
} 