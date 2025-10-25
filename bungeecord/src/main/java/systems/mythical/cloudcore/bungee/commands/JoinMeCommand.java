package systems.mythical.cloudcore.bungee.commands;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.Setting;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.utils.ImageUtils;
import systems.mythical.cloudcore.joinme.JoinMeTokenManager;

public class JoinMeCommand extends Command {
    private final MessageManager messageManager;
    private final SettingsManager settingsManager;
    private final Setting<Integer> cooldownSetting;
    private final Map<UUID, Long> cooldowns;
    private final CloudCoreBungee plugin;
    private final File cacheDir;
    private final JoinMeTokenManager tokenManager;

    public JoinMeCommand(CloudCoreBungee plugin) {
        super("joinme");
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.settingsManager = SettingsManager.getInstance(
                CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger()), plugin.getLogger());
        this.cooldowns = new HashMap<>();
        this.plugin = plugin;
        this.tokenManager = JoinMeTokenManager.getInstance();

        // Initialize cache directory
        this.cacheDir = new File(plugin.getDataFolder(), "caches/joinme/img");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Initialize settings with default values
        this.cooldownSetting = new CommonSettings.IntegerSetting(Settings.JOINME_COOLDOWN, 60); // Default 60 seconds
        
        // Register settings
        settingsManager.registerSetting(cooldownSetting);

        // Start token cleanup task
        plugin.getProxy().getScheduler().schedule(plugin, tokenManager::cleanupExpiredTokens, 5, 5, TimeUnit.MINUTES);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used in-game.");
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        if (!player.hasPermission(Permissions.JOINME_USE)) {
            player.sendMessage(messageManager.getColoredMessage(Messages.JOINME_NO_PERMISSION));
            return;
        }

        int cooldown = settingsManager.getValue(cooldownSetting);
        UUID playerId = player.getUniqueId();

        // Check if player is on cooldown
        if (cooldown > 0) {
            Long lastUsed = cooldowns.get(playerId);
            if (lastUsed != null) {
                long timeLeft = (lastUsed + (cooldown * 1000L)) - System.currentTimeMillis();
                if (timeLeft > 0) {
                    // Player is on cooldown
                    if (!player.hasPermission(Permissions.JOINME_USE_BYPASS_COOLDOWN)) {
                        // Format time left into seconds
                        int secondsLeft = (int) (timeLeft / 1000);
                        player.sendMessage(messageManager.getColoredMessage(Messages.JOINME_COOLDOWN, secondsLeft));
                        return;
                    }
                }
            }
        }

        // Update cooldown
        cooldowns.put(playerId, System.currentTimeMillis());
        
        BufferedImage head = ImageUtils.fetchHead(player.getUniqueId(), player.getName(), cacheDir, plugin.getLogger());
        String userName = player.getName();
        String serverName = player.getServer().getInfo().getName();

        if (head != null && userName != null) {
            // Convert head to chat pixel art with improved settings
            String[][] chatColors = ImageUtils.toChatColorArray(head, 8); // 8 pixels tall
            String[] headLines = ImageUtils.toImgMessage(chatColors, '█'); // Using full block character
            
            for (ProxiedPlayer onlinePlayer : plugin.getProxy().getPlayers()) {
                // Send a blank line first for spacing
                onlinePlayer.sendMessage(new TextComponent(""));
                
                // For each line of the head, combine with the appropriate text
                String[] lines = headLines;
                if (lines.length >= 3) {
                    // First line: head + "player is playing on"
                    TextComponent line1 = new TextComponent(lines[0]);
                    line1.addExtra("          "); // Add proper spacing
                    line1.addExtra(userName+" ");
                    line1.addExtra(messageManager.getColoredMessage(Messages.JOINME_IS_PLAYING_ON));
                    onlinePlayer.sendMessage(line1);
                    
                    // Second line: head + server name
                    TextComponent line2 = new TextComponent(lines[1]);
                    line2.addExtra("          "); // Add proper spacing
                    TextComponent serverText = new TextComponent(messageManager.getColoredMessage(Messages.JOINME_SERVER_NAME, serverName.toUpperCase()));
                    serverText.setColor(ChatColor.GOLD);
                    serverText.setBold(true);
                    line2.addExtra(serverText);
                    onlinePlayer.sendMessage(line2);
                    
                    // Third line: head + click text
                    TextComponent line3 = new TextComponent(lines[2]);
                    line3.addExtra("          "); // Add proper spacing
                    TextComponent clickText = new TextComponent(messageManager.getColoredMessage(Messages.JOINME_CLICK_TO_JOIN));
                    clickText.setColor(ChatColor.YELLOW);
                    String token = tokenManager.generateToken(serverName, player.getUniqueId());
                    clickText.setClickEvent(new ClickEvent(Action.RUN_COMMAND, "/performjoin " + token));
                    line3.addExtra(clickText);
                    onlinePlayer.sendMessage(line3);
                    
                    // Remaining head lines if any
                    for (int i = 3; i < lines.length; i++) {
                        onlinePlayer.sendMessage(new TextComponent(lines[i]));
                    }
                }
                
                // Send a blank line for spacing
                onlinePlayer.sendMessage(new TextComponent(""));
            }
        } else {
            player.sendMessage(messageManager.getColoredMessage(Messages.JOINME_ERROR));
        }
    }

    public Optional<String> getServerFromToken(String token) {
        return tokenManager.getServerFromToken(token);
    }

    public boolean validateAndConsumeToken(String token, UUID player) {
        return tokenManager.validateAndConsumeToken(token, player);
    }
}
