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
import systems.mythical.cloudcore.messages.MessageManager;

public class Social implements SimpleCommand {
    private final String url;
    private final String name;
    private final MessageManager messageManager;
    private final LegacyComponentSerializer legacySerializer;

    public Social(String name, String url, CloudCoreVelocity plugin) {
        this.url = url;
        this.name = name;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();

        // Create clickable message with URL
        Component message = Component.text()
            .append(legacySerializer.deserialize(
                messageManager.getColoredMessage(Messages.SOCIAL_LINK_FORMAT)
                    .replace("%name%", name)))
            .append(Component.text(url, TextColor.color(0x00FFFF)))
            .clickEvent(ClickEvent.openUrl(url))
            .hoverEvent(HoverEvent.showText(legacySerializer.deserialize(
                messageManager.getColoredMessage(Messages.SOCIAL_HOVER_TEXT)
                    .replace("%name%", name))))
            .build();

        source.sendMessage(message);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return true; // All players can use this command
    }
} 