package systems.mythical.cloudcore.bungee.commands;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemManager;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class CloudCoreCommand extends Command implements TabExecutor {
    private final CloudCoreBungee plugin;
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private final MaintenanceSystemManager maintenanceSystemManager;

    public CloudCoreCommand(CloudCoreBungee plugin) {
        super("cloudcore", null, "cc");
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.maintenanceSystemManager = MaintenanceSystemManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 0) {
            handleVersion(sender);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;

            case "clearcache":
                handleClearCache(sender);
                break;

            case "version":
                handleVersion(sender);
                break;

            case "maintenance":
                handleMaintenance(sender, args);
                break;

            case "settings":
                handleSettings(sender, args);
                break;

            default:
                handleVersion(sender);
                break;
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission(Permissions.ADMIN_RELOAD)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION))));
            return;
        }
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
            messageManager.getColoredMessage(Messages.ADMIN_RELOAD_START))));
        try {
            // Reload settings
            cloudSettings.refreshSettings();
            // Reload messages
            messageManager.refreshMessages();
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_RELOAD_SUCCESS))));
        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading CloudCore: " + e.getMessage());
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_RELOAD_ERROR))));
        }
    }

    private void handleClearCache(CommandSender sender) {
        if (!sender.hasPermission(Permissions.ADMIN_CLEAR_CACHE)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION))));
            return;
        }
        try {
            // Clear settings cache
            cloudSettings.refreshSettings();
            // Clear message cache
            messageManager.refreshMessages();
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_CACHE_CLEARED))));
        } catch (Exception e) {
            plugin.getLogger().severe("Error clearing cache: " + e.getMessage());
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_RELOAD_ERROR))));
        }
    }

    private void handleVersion(CommandSender sender) {
        if (!sender.hasPermission(Permissions.ADMIN_VERSION)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION))));
            return;
        }
        String version = plugin.getDescription().getVersion();
        sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
            messageManager.getColoredMessage(Messages.ADMIN_VERSION_INFO, version))));
    }

    private void handleMaintenance(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.MAINTENANCE_MANAGE)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION))));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE))));
            return;
        }

        CloudSettings settings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
            case "enable":
                settings.setSetting(Settings.MAINTENANCE_MODE, "true");
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_ENABLED))));
                break;

            case "off":
            case "disable":
                settings.setSetting(Settings.MAINTENANCE_MODE, "false");
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_DISABLED))));
                break;

            case "add":
                if (args.length < 3) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE))));
                    return;
                }
                String playerToAdd = args[2];
                ProxiedPlayer targetAdd = plugin.getProxy().getPlayer(playerToAdd);
                if (targetAdd == null) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_NOT_FOUND))));
                    return;
                }
                maintenanceSystemManager.addMaintenance(targetAdd.getUniqueId());
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_ADDED, playerToAdd))));
                break;

            case "remove":
                if (args.length < 3) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE))));
                    return;
                }
                String playerToRemove = args[2];
                ProxiedPlayer targetRemove = plugin.getProxy().getPlayer(playerToRemove);
                if (targetRemove == null) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_NOT_FOUND))));
                    return;
                }
                maintenanceSystemManager.removeMaintenance(targetRemove.getUniqueId());
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_REMOVED, playerToRemove))));
                break;

            case "list":
                String allowedList = maintenanceSystemManager.getMaintenanceList();
                if (allowedList.isEmpty()) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.MAINTENANCE_LIST_EMPTY))));
                    return;
                }
                List<String> playerNames = Arrays.stream(allowedList.split(","))
                    .map(UUID::fromString)
                    .map(uuid -> plugin.getProxy().getPlayer(uuid))
                    .filter(player -> player != null)
                    .map(ProxiedPlayer::getName)
                    .collect(Collectors.toList());
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_LIST, String.join(", ", playerNames)))));
                break;

            default:
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE))));
                break;
        }
    }

    private void handleSettings(CommandSender sender, String[] args) {
        if (!sender.hasPermission(Permissions.SETTINGS_MANAGE)) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION))));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                messageManager.getColoredMessage(Messages.SETTINGS_USAGE))));
            return;
        }

        CloudSettings settings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        String action = args[1].toLowerCase();

        switch (action) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.SETTINGS_USAGE))));
                    return;
                }
                String settingName = args[2];
                String settingValue = args[3];
                settings.setSetting(settingName, settingValue);
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.SETTINGS_SET, settingName, settingValue))));
                break;

            case "get":
                if (args.length < 3) {
                    sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                        messageManager.getColoredMessage(Messages.SETTINGS_USAGE))));
                    return;
                }
                String setting = args[2];
                String value = settings.getSetting(setting);
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.SETTINGS_GET, setting, value))));
                break;

            default:
                sender.sendMessage(new TextComponent(ChatColor.translateAlternateColorCodes('&',
                    messageManager.getColoredMessage(Messages.SETTINGS_USAGE))));
                break;
        }
    }

    @Override
    public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
        final List<String> suggestions = new ArrayList<>();

        if (args.length == 1) {
            if (sender.hasPermission(Permissions.ADMIN_RELOAD)) {
                suggestions.add("reload");
            }
            if (sender.hasPermission(Permissions.ADMIN_CLEAR_CACHE)) {
                suggestions.add("clearcache");
            }
            if (sender.hasPermission(Permissions.ADMIN_VERSION)) {
                suggestions.add("version");
            }
            if (sender.hasPermission(Permissions.MAINTENANCE_MANAGE)) {
                suggestions.add("maintenance");
            }
            if (sender.hasPermission(Permissions.SETTINGS_MANAGE)) {
                suggestions.add("settings");
            }
            // filter by partial first-argument input
            filterByPrefixInPlace(suggestions, args[0]);
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "maintenance":
                    if (sender.hasPermission(Permissions.MAINTENANCE_MANAGE)) {
                        suggestions.addAll(Arrays.asList("on", "off", "add", "remove", "list"));
                    }
                    break;
                case "settings":
                    if (sender.hasPermission(Permissions.SETTINGS_MANAGE)) {
                        suggestions.addAll(Arrays.asList("set", "get"));
                    }
                    break;
            }
            // filter by partial second-argument input
            filterByPrefixInPlace(suggestions, args[1]);
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            if (subCommand.equals("maintenance") && (action.equals("add") || action.equals("remove"))) {
                // Suggest online players
                plugin.getProxy().getPlayers().forEach(player -> suggestions.add(player.getName()));
                filterByPrefixInPlace(suggestions, args[2]);
            } else if (subCommand.equals("settings") && action.equals("set")) {
                // Suggest available settings
                suggestions.addAll(Settings.getGlobalSettings());
                filterByPrefixInPlace(suggestions, args[2]);
            } else if (subCommand.equals("settings") && action.equals("get")) {
                // Suggest available settings for get
                suggestions.addAll(Settings.getGlobalSettings());
                filterByPrefixInPlace(suggestions, args[2]);
            }
        }

        return suggestions;
    }

    private void filterByPrefixInPlace(List<String> items, String prefix) {
        String lowerPrefix = prefix == null ? "" : prefix.toLowerCase();
        if (lowerPrefix.isEmpty()) {
            return;
        }
        items.removeIf(item -> item == null || !item.toLowerCase().startsWith(lowerPrefix));
    }
} 