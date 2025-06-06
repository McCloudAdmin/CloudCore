package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
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

public class PanelCommand implements SimpleCommand {
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private final UserManager userManager;
    private final LegacyComponentSerializer legacySerializer;

    public PanelCommand(CloudCoreVelocity plugin) {
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_PLAYERS_ONLY)));
            return;
        }

        Player player = (Player) source;

        if (args.length == 0) {
            sendUsage(source);
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
                sendUsage(source);
                break;
        }
    }

    private void handleLogin(Player player) {
        Optional<User> userOpt = userManager.getUserByUuid(player.getUniqueId());
        if (userOpt.isEmpty()) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_NO_ACCOUNT)));
            return;
        }

        User user = userOpt.get();
        String appUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL);
        String loginUrl = appUrl + "/auth/login?token=" + java.util.Base64.getEncoder().encodeToString(user.getToken().getBytes())+"&performLogin=true";

        // Create a clickable link component with translated message
        Component message = Component.empty()
            .append(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_LOGIN_URL)))
            .append(Component.text(loginUrl, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl(loginUrl)));
        
        player.sendMessage(message);
    }

    private void handleReset(Player player) {
        Optional<User> userOpt = userManager.getUserByUuid(player.getUniqueId());
        if (userOpt.isEmpty()) {
            player.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_NO_ACCOUNT)));
            return;
        }

        String token = CloudCoreLogic.generateSecureStringToken(userOpt.get().getUsername(), userOpt.get().getUuid().toString());
        userOpt.get().setToken(token);
        userManager.updateUser(userOpt.get());
        
        String appUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL);
        String loginUrl = appUrl + "/auth/login?token=" + java.util.Base64.getEncoder().encodeToString(token.getBytes())+"&performLogin=true";

        // Create a clickable link component with translated message
        Component message = Component.empty()
            .append(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_RESET_MSG)))
            .append(Component.text(loginUrl, NamedTextColor.AQUA)
                .clickEvent(ClickEvent.openUrl(loginUrl)));
        
        player.sendMessage(message);
    }

    private void sendUsage(CommandSource source) {
        source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PANEL_USAGE)));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            suggestions.add("login");
            suggestions.add("reset");
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // All players can use this command
    }
} 