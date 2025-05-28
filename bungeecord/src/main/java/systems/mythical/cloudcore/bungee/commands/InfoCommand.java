package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
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

public class InfoCommand extends Command implements TabExecutor {
    private final MessageManager messageManager;
    private final UserManager userManager;
    private final SimpleDateFormat dateFormat;

    public InfoCommand(CloudCoreBungee plugin) {
        super("info");
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.userManager = UserManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof ProxiedPlayer)) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.INFORMATION_USAGE_OTHER)));
            return;
        }

        ProxiedPlayer player = (ProxiedPlayer) sender;
        String targetName;

        if (args.length == 0) {
            targetName = player.getName();
        } else {
            if (!player.hasPermission(Permissions.ADMIN_INFO)) {
                sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.INFORMATION_USAGE_SELF)));
                return;
            }
            targetName = args[0];
        }

        Optional<User> userOpt = userManager.getUserByUsername(targetName);
        if (userOpt.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.INFORMATION_NOT_FOUND)));
            return;
        }

        User user = userOpt.get();
        displayUserInfo(sender, user, player);
    }

    private void displayUserInfo(CommandSender sender, User user, ProxiedPlayer viewer) {
        // Header
        sender.sendMessage(TextComponent.fromLegacyText(messageManager.getColoredMessage(Messages.INFORMATION_HEADER)));
        
        // Basic Information
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_NAME), user.getUsername())));
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_RANK), user.getUserGroup())));
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_VERIFIED), user.isVerified() ? messageManager.getColoredMessage(Messages.STATUS_YES) : messageManager.getColoredMessage(Messages.STATUS_NO))));
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_SUPPORT_PIN), user.getSupportPin())));
            
        
        // Server Information
        if (user.getUserConnectedServerName() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_SERVER), user.getUserConnectedServerName())));
        }
        
        // Online Status
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_ONLINE), 
            user.isUserOnline() ? messageManager.getColoredMessage(Messages.STATUS_YES) : messageManager.getColoredMessage(Messages.STATUS_NO))));
        
        // Version
        if (user.getUserVersion() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_VERSION), user.getUserVersion())));
        }
        
        // Login Information
        if (user.getFirstSeen() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_FIRST_LOGIN), 
                dateFormat.format(Date.from(user.getFirstSeen().toInstant(java.time.ZoneOffset.UTC))))));
        }
        if (user.getLastSeen() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_LAST_LOGIN), 
                dateFormat.format(Date.from(user.getLastSeen().toInstant(java.time.ZoneOffset.UTC))))));
        }

        // Credits
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_CREDITS), user.getCredits())));

        // IP
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_IP), user.getLastIp())));

        // Social Links
        if (user.getGithubUsername() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_GITHUB), user.getGithubUsername())));
        }
        if (user.getDiscordUsername() != null) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_DISCORD), user.getDiscordUsername())));
        }
        
        // Ban/Mute Status
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_BAN_STATUS), 
            user.isBanned() ? messageManager.getColoredMessage(Messages.STATUS_BANNED) + (user.getBanReason() != null ? " (" + user.getBanReason() + ")" : "") : messageManager.getColoredMessage(Messages.STATUS_NOT_BANNED))));
        sender.sendMessage(TextComponent.fromLegacyText(String.format(
            messageManager.getColoredMessage(Messages.INFORMATION_MUTE_STATUS), 
            user.isLocked() ? messageManager.getColoredMessage(Messages.STATUS_MUTED) : messageManager.getColoredMessage(Messages.STATUS_NOT_MUTED))));

        // Other Accounts
        List<User> otherAccounts = userManager.getOtherAccounts(user);
        if (!otherAccounts.isEmpty()) {
            sender.sendMessage(TextComponent.fromLegacyText(String.format(
                messageManager.getColoredMessage(Messages.INFORMATION_OTHER_ACCOUNTS), otherAccounts.size())));
            for (User otherAccount : otherAccounts) {
                sender.sendMessage(TextComponent.fromLegacyText(String.format(
                    messageManager.getColoredMessage(Messages.INFORMATION_OTHER_ACCOUNT), 
                    otherAccount.getUsername(),
                    otherAccount.isUserOnline() ? messageManager.getColoredMessage(Messages.STATUS_ONLINE) : messageManager.getColoredMessage(Messages.STATUS_OFFLINE))));
            }
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        List<String> suggestions = new ArrayList<>();
        if (args.length == 1 && sender.hasPermission(Permissions.ADMIN_INFO)) {
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