package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
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

public class ChatlogCommand extends Command implements TabExecutor {
    private static final long COOLDOWN_MILLIS = 120_000; // 120 seconds
    private static final Map<UUID, Long> cooldowns = new ConcurrentHashMap<>();
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final ChatLogManager chatLogManager;
    private final CloudSettings cloudSettings;

    public ChatlogCommand(CloudCoreBungee plugin) {
        super("chatlog");
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.chatLogManager = ChatLogManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (sender instanceof ProxiedPlayer) {
            ProxiedPlayer player = (ProxiedPlayer) sender;
            UUID uuid = player.getUniqueId();
            long now = System.currentTimeMillis();
            Long lastUsed = cooldowns.get(uuid);
            if (lastUsed != null && (now - lastUsed) < COOLDOWN_MILLIS) {
                long secondsLeft = (COOLDOWN_MILLIS - (now - lastUsed)) / 1000;
                sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.CHATLOG_COOLDOWN, secondsLeft)));
                return;
            }
            cooldowns.put(uuid, now);
            if (!player.hasPermission(Permissions.CHATLOG_COMMAND)) {
                sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
                return;
            }
        }
        if (args.length != 1) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.CHATLOG_USAGE)));
            return;
        }

        String targetName = args[0];
        Optional<User> userOpt = userManager.getUserByUsername(targetName);
        Optional<User> senderOpt = userManager.getUserByUuid(((ProxiedPlayer) sender).getUniqueId());
        
        if (userOpt.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.CHATLOG_NOT_FOUND)));
            return;
        }

        User user = userOpt.get();
        User senderUser = senderOpt.get();
        var chatLog = chatLogManager.createChatLog(user, senderUser);

        if (chatLog == null) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.CHATLOG_NO_MESSAGES)));
            return;
        }

        String chatlogUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL) + "/chatlog/" + chatLog.getId();

        // Create clickable message with URL
        TextComponent message = new TextComponent(messageManager.getColoredMessage(Messages.CHATLOG_LINK));
        TextComponent url = new TextComponent(chatlogUrl);
        
        url.setColor(ChatColor.AQUA);
        url.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, chatlogUrl));
        url.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, 
            new ComponentBuilder("Click to view chat log").color(ChatColor.GRAY).create()));

        message.addExtra(url);
        sender.sendMessage(message);
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            for (User user : userManager.getAllUsers()) {
                if (user.getUsername().toLowerCase().startsWith(partialName)) {
                    suggestions.add(user.getUsername());
                }
            }
        }
        return suggestions;
    }
} 