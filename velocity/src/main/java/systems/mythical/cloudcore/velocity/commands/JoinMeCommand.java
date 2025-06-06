package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.Setting;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.utils.ImageUtils;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.joinme.JoinMeTokenManager;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class JoinMeCommand implements SimpleCommand {
    private final CloudCoreVelocity plugin;
    private final MessageManager messageManager;
    private final SettingsManager settingsManager;
    private final Setting<Integer> cooldownSetting;
    private final Map<UUID, Long> cooldowns;
    private final File cacheDir;
    private final JoinMeTokenManager tokenManager;

    public JoinMeCommand(CloudCoreVelocity plugin, Path dataDirectory) {
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.settingsManager = SettingsManager.getInstance(
                CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger()), plugin.getLogger());
        this.cooldowns = new HashMap<>();
        this.tokenManager = JoinMeTokenManager.getInstance();

        // Initialize cache directory
        String dir = dataDirectory.toFile().toString();
        dir = dir.replaceAll("(?i)cloudcore", "CloudCore");
        this.cacheDir = new File(dir, "caches/joinme/img");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }

        // Initialize settings with default values
        this.cooldownSetting = new CommonSettings.IntegerSetting(Settings.JOINME_COOLDOWN, 60); // Default 60 seconds
        
        // Register settings
        settingsManager.registerSetting(cooldownSetting);

        // Start token cleanup task
        plugin.getServer().getScheduler()
            .buildTask(plugin, tokenManager::cleanupExpiredTokens)
            .delay(5, TimeUnit.MINUTES)
            .repeat(5, TimeUnit.MINUTES)
            .schedule();
    }

    @Override
    public void execute(final Invocation invocation) {
        CommandSource source = invocation.source();

        if (!(source instanceof Player)) {
            source.sendMessage(Component.text("This command can only be used in-game.").color(TextColor.color(255, 85, 85)));
            return;
        }

        Player player = (Player) source;
        if (!player.hasPermission(Permissions.JOINME_USE)) {
            player.sendMessage(Component.text(messageManager.getColoredMessage(Messages.JOINME_NO_PERMISSION)));
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
                        player.sendMessage(Component.text(messageManager.getColoredMessage(Messages.JOINME_COOLDOWN, secondsLeft)));
                        return;
                    }
                }
            }
        }

        // Update cooldown
        cooldowns.put(playerId, System.currentTimeMillis());

        Optional<ServerConnection> currentServer = player.getCurrentServer();
        if (!currentServer.isPresent()) {
            player.sendMessage(Component.text("You must be on a server to use this command.").color(TextColor.color(255, 85, 85)));
            return;
        }

        RegisteredServer server = currentServer.get().getServer();
        
        BufferedImage head = ImageUtils.fetchHead(player.getUniqueId(), player.getUsername(), cacheDir, plugin.getLogger());
        String userName = player.getUsername();
        String serverName = server.getServerInfo().getName();

        if (head != null && userName != null) {
            // Convert head to chat pixel art with improved settings
            String[][] chatColors = ImageUtils.toChatColorArray(head, 8); // 8 pixels tall
            String[] headLines = ImageUtils.toImgMessage(chatColors, '█'); // Using full block character
            
            for (Player onlinePlayer : plugin.getServer().getAllPlayers()) {
                // Send a blank line first for spacing
                onlinePlayer.sendMessage(Component.text(""));
                
                // For each line of the head, combine with the appropriate text
                String[] lines = headLines;
                if (lines.length >= 3) {
                    // First line: head + "player is playing on"
                    Component line1 = Component.text()
                        .append(Component.text(lines[0]))
                        .append(Component.text("          ")) // Add proper spacing
                        .append(Component.text(userName + " ").color(TextColor.color(85, 255, 85)))
                        .append(Component.text(messageManager.getColoredMessage(Messages.JOINME_IS_PLAYING_ON))
                                .color(TextColor.color(170, 170, 170)))
                        .build();
                    onlinePlayer.sendMessage(line1);
                    
                    // Second line: head + server name
                    Component line2 = Component.text()
                        .append(Component.text(lines[1]))
                        .append(Component.text("          ")) // Add proper spacing
                        .append(Component.text(messageManager.getColoredMessage(Messages.JOINME_SERVER_NAME, serverName.toUpperCase()))
                            .color(TextColor.color(255, 170, 0)))
                        .decorate(TextDecoration.BOLD)
                        .build();
                    onlinePlayer.sendMessage(line2);
                    
                    // Third line: head + click text
                    String token = tokenManager.generateToken(serverName, player.getUniqueId());
                    Component line3 = Component.text()
                        .append(Component.text(lines[2]))
                        .append(Component.text("          ")) // Add proper spacing
                        .append(Component.text(messageManager.getColoredMessage(Messages.JOINME_CLICK_TO_JOIN))
                            .color(TextColor.color(255, 255, 85))
                            .clickEvent(ClickEvent.clickEvent(ClickEvent.Action.RUN_COMMAND, "/performjoin " + token)))
                        .build();
                    onlinePlayer.sendMessage(line3);
                    
                    // Remaining head lines if any
                    for (int i = 3; i < lines.length; i++) {
                        onlinePlayer.sendMessage(Component.text(lines[i]));
                    }
                }
                
                // Send a blank line for spacing
                onlinePlayer.sendMessage(Component.text(""));
            }
        } else {
            player.sendMessage(Component.text(messageManager.getColoredMessage(Messages.JOINME_ERROR)));
        }
    }

    public Optional<String> getServerFromToken(String token) {
        return tokenManager.getServerFromToken(token);
    }

    public boolean validateAndConsumeToken(String token, UUID player) {
        return tokenManager.validateAndConsumeToken(token, player);
    }
}