package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileCommand implements SimpleCommand {
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final String appUrl;
    private final LegacyComponentSerializer legacySerializer;

    public ProfileCommand(CloudCoreVelocity plugin) {
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        CloudSettings cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.appUrl = cloudSettings.getSetting(Settings.GLOBAL_APP_URL);
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length != 1) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PROFILE_USAGE)));
            return;
        }

        String targetName = args[0];
        Optional<User> userOpt = userManager.getUserByUsername(targetName);
        
        if (userOpt.isEmpty()) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.PROFILE_NOT_FOUND)));
            return;
        }

        User user = userOpt.get();
        String profileUrl = appUrl + "/profile/" + user.getUuid();

        // Create clickable message with URL
        Component message = Component.text(messageManager.getColoredMessage(Messages.PROFILE_LINK))
            .append(Component.text(profileUrl)
                .clickEvent(ClickEvent.openUrl(profileUrl))
                .hoverEvent(HoverEvent.showText(Component.text("Click to view profile", TextColor.color(0x808080)))));
        
        source.sendMessage(message);
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

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

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // All players can use this command
    }
} 