package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.users.User;
import systems.mythical.cloudcore.users.UserManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class InfoCommand implements SimpleCommand {
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final SimpleDateFormat dateFormat;
    private final LegacyComponentSerializer legacySerializer;

    public InfoCommand(CloudCoreVelocity plugin) {
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (!(source instanceof Player)) {
            source.sendMessage(
                    legacySerializer.deserialize(messageManager.getColoredMessage(Messages.INFORMATION_USAGE_OTHER)));
            return;
        }

        Player player = (Player) source;
        String targetName;

        if (args.length == 0) {
            targetName = player.getUsername();
        } else {
            if (!player.hasPermission(Permissions.ADMIN_INFO)) {
                source.sendMessage(legacySerializer
                        .deserialize(messageManager.getColoredMessage(Messages.INFORMATION_USAGE_SELF)));
                return;
            }
            targetName = args[0];
        }

        Optional<User> userOpt = userManager.getUserByUsername(targetName);
        if (userOpt.isEmpty()) {
            source.sendMessage(
                    legacySerializer.deserialize(messageManager.getColoredMessage(Messages.INFORMATION_NOT_FOUND)));
            return;
        }

        User user = userOpt.get();
        displayUserInfo(source, user, player);
    }

    private void displayUserInfo(CommandSource sender, User user, Player viewer) {
        // Header
        sender.sendMessage(Component.text(messageManager.getColoredMessage(Messages.INFORMATION_HEADER)));
        
        // Basic Information
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_NAME), user.getUsername())));
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_RANK), user.getUserGroup())));
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_VERIFIED), user.isVerified() ? messageManager.getColoredMessage(Messages.STATUS_YES) : messageManager.getColoredMessage(Messages.STATUS_NO))));
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_SUPPORT_PIN), user.getSupportPin())));

        // Server Information
        if (user.getUserConnectedServerName() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_SERVER), user.getUserConnectedServerName())));
        }

        // Online Status
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_ONLINE),
                user.isUserOnline() ? messageManager.getColoredMessage(Messages.STATUS_YES)
                        : messageManager.getColoredMessage(Messages.STATUS_NO))));

        // Version
        if (user.getUserVersion() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_VERSION), user.getUserVersion())));
        }

        // Login Information
        if (user.getFirstSeen() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_FIRST_LOGIN),
                    dateFormat.format(Date.from(user.getFirstSeen().toInstant(java.time.ZoneOffset.UTC))))));
        }
        if (user.getLastSeen() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_LAST_LOGIN),
                    dateFormat.format(Date.from(user.getLastSeen().toInstant(java.time.ZoneOffset.UTC))))));
        }
        // Credits
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_CREDITS), user.getCredits())));

        // IP
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_IP), user.getLastIp())));
        // Social Links
        if (user.getGithubUsername() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_GITHUB), user.getGithubUsername())));
        }
        if (user.getDiscordUsername() != null) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_DISCORD), user.getDiscordUsername())));
        }

        // Ban/Mute Status
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_BAN_STATUS),
                user.isBanned()
                        ? messageManager.getColoredMessage(Messages.STATUS_BANNED)
                                + (user.getBanReason() != null ? " (" + user.getBanReason() + ")" : "")
                        : messageManager.getColoredMessage(Messages.STATUS_NOT_BANNED))));
        sender.sendMessage(Component.text(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_MUTE_STATUS),
                user.isLocked() ? messageManager.getColoredMessage(Messages.STATUS_MUTED)
                        : messageManager.getColoredMessage(Messages.STATUS_NOT_MUTED))));

        // Other Accounts
        List<User> otherAccounts = userManager.getOtherAccounts(user);
        if (!otherAccounts.isEmpty()) {
            sender.sendMessage(Component.text(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_OTHER_ACCOUNTS), otherAccounts.size())));
            for (User otherAccount : otherAccounts) {
                sender.sendMessage(Component.text(String.format(
                        messageManager.getColoredMessage(Messages.INFORMATION_OTHER_ACCOUNT),
                        otherAccount.getUsername(),
                        otherAccount.isUserOnline() ? messageManager.getColoredMessage(Messages.STATUS_ONLINE)
                                : messageManager.getColoredMessage(Messages.STATUS_OFFLINE))));
            }
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();
        CommandSource source = invocation.source();

        if (args.length == 1 && source.hasPermission(Permissions.ADMIN_INFO)) {
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