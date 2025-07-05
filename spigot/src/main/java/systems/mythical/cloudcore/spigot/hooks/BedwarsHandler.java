package systems.mythical.cloudcore.spigot.hooks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.andrei1058.bedwars.api.BedWars;

public class BedwarsHandler {

    private BedWars bedwarsAPI;

    public BedwarsHandler() {
        bedwarsAPI = Bukkit.getServicesManager().getRegistration(BedWars.class).getProvider();
    }

    public int getLevel(Player player) {
        return bedwarsAPI.getLevelsUtil().getPlayerLevel(player);
    }

    /**
     * Get player's total kills in BedWars
     * 
     * @param player The player to check
     * @return The total kills
     */
    public int getKills(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerKills(player.getUniqueId());
    }

    /**
     * Get player's total deaths in BedWars
     * 
     * @param player The player to check
     * @return The total deaths
     */
    public int getDeaths(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerDeaths(player.getUniqueId());
    }

    /**
     * Get player's total wins in BedWars
     * 
     * @param player The player to check
     * @return The total wins
     */
    public int getWins(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerWins(player.getUniqueId());
    }

    /**
     * Get player's total losses in BedWars
     * 
     * @param player The player to check
     * @return The total losses
     */
    public int getLosses(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerLoses(player.getUniqueId());
    }

    /**
     * Get player's total final kills in BedWars
     * 
     * @param player The player to check
     * @return The total final kills
     */
    public int getFinalKills(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerFinalKills(player.getUniqueId());
    }

    /**
     * Get player's total final deaths in BedWars
     * 
     * @param player The player to check
     * @return The total final deaths
     */
    public int getFinalDeaths(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerFinalDeaths(player.getUniqueId());
    }

    /**
     * Get player's total bed breaks in BedWars
     * 
     * @param player The player to check
     * @return The total bed breaks
     */
    public int getBedBreaks(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerBedsDestroyed(player.getUniqueId());
    }

    /**
     * Get player's total games played in BedWars
     * 
     * @param player The player to check
     * @return The total games played
     */
    public int getGamesPlayed(Player player) {
        return bedwarsAPI.getStatsUtil().getPlayerGamesPlayed(player.getUniqueId());
    }
}