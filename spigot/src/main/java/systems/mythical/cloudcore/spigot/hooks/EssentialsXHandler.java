package systems.mythical.cloudcore.spigot.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import org.bukkit.entity.Player;

public class EssentialsXHandler {
    private final Essentials essentials;

    public EssentialsXHandler(Essentials essentials) {
        this.essentials = essentials;
    }

    /**
     * Check if a player is AFK according to EssentialsX
     * @param player The player to check
     * @return true if the player is AFK, false otherwise
     */
    public boolean isAfk(Player player) {
        User user = essentials.getUser(player);
        return user != null && user.isAfk();
    }

    /**
     * Get the GEO location of a player's IP address
     * @param player The player to check
     * @return The GEO location string, or null if not available
     */
    public String getGeoLocation(Player player) {
        User user = essentials.getUser(player);
        if (user != null) {
            String geoLocation = user.getGeoLocation();
            return geoLocation != null ? geoLocation : "Unknown";
        }
        return "Unknown";
    }

    /**
     * Get the Essentials instance
     * @return The Essentials instance
     */
    public Essentials getEssentials() {
        return essentials;
    }
} 