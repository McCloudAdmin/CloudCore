package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.chatlog.ChatLogManager;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ChatlogCommand implements SimpleCommand {
    private static final long COOLDOWN_MILLIS = 120_000; // 120 seconds
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final ChatLogManager chatLogManager;
    private final CloudSettings cloudSettings;
    private final LegacyComponentSerializer legacySerializer;

    public ChatlogCommand(CloudCoreVelocity plugin) {
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.chatLogManager = ChatLogManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(legacySerializer.deserialize("&cOnly players can use this command."));
            return;
        }
        Player player = (Player) source;
        UUID uuid = player.getUniqueId();

        // Permission check (replace with your actual permission string)
        if (!player.hasPermission(Permissions.CHATLOG_COMMAND)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }

        if (args.length != 1) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CHATLOG_USAGE)));
            return;
        }

        String targetName = args[0];
        Optional<User> userOpt = userManager.getUserByUsername(targetName);
        Optional<User> senderOpt = userManager.getUserByUuid(uuid);

        if (userOpt.isEmpty()) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CHATLOG_NOT_FOUND)));
            return;
        }
        if (senderOpt.isEmpty()) {
            source.sendMessage(legacySerializer.deserialize("&cCould not find your user profile."));
            return;
        }

        // Check cooldown only right before actually creating a chatlog
        long now = System.currentTimeMillis();
        Long lastUsed = cooldowns.get(uuid);
        if (lastUsed != null && (now - lastUsed) < COOLDOWN_MILLIS) {
            long secondsLeft = (COOLDOWN_MILLIS - (now - lastUsed)) / 1000;
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CHATLOG_COOLDOWN, secondsLeft)));
            return;
        }

        User user = userOpt.get();
        User senderUser = senderOpt.get();
        var chatLog = chatLogManager.createChatLog(user, senderUser);

        if (chatLog == null) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CHATLOG_NO_MESSAGES)));
            return;
        }

        // Only set cooldown after a successful chatlog creation
        cooldowns.put(uuid, now);

        String chatlogUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL) + "/chatlog/" + chatLog.getId();

        // Create clickable message with URL
        Component message = Component.text()
            .append(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.CHATLOG_LINK)))
            .append(Component.text(chatlogUrl)
                .color(NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl(chatlogUrl))
                .hoverEvent(HoverEvent.showText(Component.text("Click to view chat log")
                    .color(TextColor.color(0x808080)))))
            .build();
        source.sendMessage(message);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            CommandSource source = invocation.source();
            if (source instanceof Player) {
                Player player = (Player) source;
                // Players connected to the same server (current server/instance) as the command sender
                player.getCurrentServer().ifPresent(current -> {
                    current.getServer().getPlayersConnected().forEach(p -> {
                        String name = p.getUsername();
                        if (name != null && name.toLowerCase().startsWith(partialName)) {
                            suggestions.add(name);
                        }
                    });
                });
            }
        }
        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // All players can use this command
    }
} 