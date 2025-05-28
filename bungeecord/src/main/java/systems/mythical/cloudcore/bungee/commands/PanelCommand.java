package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.core.CloudCoreLogic;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PanelCommand extends Command implements TabExecutor {
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private final UserManager userManager;

    public PanelCommand(CloudCoreBungee plugin) {
        super("panel");
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_PLAYERS_ONLY)));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;

        if (args.length == 0) {
            sendUsage(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "login":
                handleLogin(player);
                break;
            case "reset":
                handleReset(player);
                break;
            default:
                sendUsage(sender);
                break;
        }
    }

    private void handleLogin(ProxiedPlayer player) {
        Optional<User> userOpt = userManager.getUserByUuid(player.getUniqueId());
        if (userOpt.isEmpty()) {
            player.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_NO_ACCOUNT)));
            return;
        }

        User user = userOpt.get();
        String appUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL);
        String loginUrl = appUrl + "/auth/login?token=" + user.getToken();

        // Create a clickable link component with translated message
        ComponentBuilder builder = new ComponentBuilder("")
            .append(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_LOGIN_URL)))
            .append(loginUrl)
            .color(ChatColor.AQUA)
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, loginUrl));

        player.sendMessage(builder.create());
    }

    private void handleReset(ProxiedPlayer player) {
        Optional<User> userOpt = userManager.getUserByUuid(player.getUniqueId());
        if (userOpt.isEmpty()) {
            player.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_NO_ACCOUNT)));
            return;
        }
        String token = CloudCoreLogic.generateSecureStringToken(userOpt.get().getUsername(), userOpt.get().getUuid().toString());
        userOpt.get().setToken(token);
        userManager.updateUser(userOpt.get());
        
        String appUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL);
        String loginUrl = appUrl + "/auth/login?token=" + token;

        // Create a clickable link component with translated message
        ComponentBuilder builder = new ComponentBuilder("")
            .append(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_RESET_MSG)))
            .append(loginUrl)
            .color(ChatColor.AQUA)
            .event(new ClickEvent(ClickEvent.Action.OPEN_URL, loginUrl));

        player.sendMessage(builder.create());
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.PANEL_USAGE)));
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            suggestions.add("login");
            suggestions.add("reset");
        }

        return suggestions;
    }
} 