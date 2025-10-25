package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.messages.MessageManager;

public class Social extends Command {
    private final String url;
    private final String name;
    private final MessageManager messageManager;

    public Social(String name, String url, CloudCoreBungee plugin) {
        super(name);
        this.url = url;
        this.name = name;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void execute(CommandSender sender, String[] args) {
        // Create clickable message with URL
        TextComponent message = new TextComponent(messageManager.getColoredMessage(Messages.SOCIAL_LINK_FORMAT)
            .replace("%name%", name));
        TextComponent urlComponent = new TextComponent(ChatColor.AQUA + url);

        urlComponent.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
        urlComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(messageManager.getColoredMessage(Messages.SOCIAL_HOVER_TEXT)
                    .replace("%name%", name)).create()));

        message.addExtra(urlComponent);
        sender.sendMessage(message);
    }
}
