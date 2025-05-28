package systems.mythical.cloudcore.velocity.commands;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.core.CloudCoreConstants.Permissions;
import systems.mythical.cloudcore.core.CloudCoreConstants.Messages;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class CloudCoreCommand implements SimpleCommand {
    private final CloudCoreVelocity plugin;
    private final MessageManager messageManager;
    private final CloudSettings cloudSettings;
    private final MaintenanceSystemManager maintenanceSystemManager;
    private final LegacyComponentSerializer legacySerializer;

    public CloudCoreCommand(CloudCoreVelocity plugin) {
        this.plugin = plugin;
        this.messageManager = MessageManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.cloudSettings = CloudSettings.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.maintenanceSystemManager = MaintenanceSystemManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
        this.legacySerializer = LegacyComponentSerializer.builder().character('&').build();
    }

    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();

        if (args.length == 0) {
            handleVersion(source);
            return;
        }

        String subCommand = args[0].toLowerCase();
        switch (subCommand) {
            case "reload":
                handleReload(source);
                break;

            case "clearcache":
                handleClearCache(source);
                break;

            case "version":
                handleVersion(source);
                break;

            case "maintenance":
                handleMaintenance(source, args);
                break;

            case "settings":
                handleSettings(source, args);
                break;

            default:
                handleVersion(source);
                break;
        }
    }

    private void handleReload(CommandSource source) {
        if (!source.hasPermission(Permissions.ADMIN_RELOAD)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }
        source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_RELOAD_START)));
        try {
            // Reload settings
            cloudSettings.refreshSettings();
            // Reload messages
            messageManager.refreshMessages();
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_RELOAD_SUCCESS)));
        } catch (Exception e) {
            plugin.getLogger().severe("Error reloading CloudCore: " + e.getMessage());
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_RELOAD_ERROR)));
        }
    }

    private void handleClearCache(CommandSource source) {
        if (!source.hasPermission(Permissions.ADMIN_CLEAR_CACHE)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }
        try {
            // Clear settings cache
            cloudSettings.refreshSettings();
            // Clear message cache
            messageManager.refreshMessages();
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_CACHE_CLEARED)));
        } catch (Exception e) {
            plugin.getLogger().severe("Error clearing cache: " + e.getMessage());
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_RELOAD_ERROR)));
        }
    }

    private void handleVersion(CommandSource source) {
        if (!source.hasPermission(Permissions.ADMIN_VERSION)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }
        String version = "1.0.0"; 
        source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(
            Messages.ADMIN_VERSION_INFO, version
        )));
    }

    private void handleMaintenance(CommandSource source, String[] args) {
        if (!source.hasPermission(Permissions.MAINTENANCE_MANAGE)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE)));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "on":
            case "enable":
                cloudSettings.setSetting(Settings.MAINTENANCE_MODE, "true");
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_ENABLED)));
                break;

            case "off":
            case "disable":
                cloudSettings.setSetting(Settings.MAINTENANCE_MODE, "false");
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_DISABLED)));
                break;

            case "add":
                if (args.length < 3) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE)));
                    return;
                }
                String playerToAdd = args[2];
                Optional<Player> targetAdd = plugin.getServer().getPlayer(playerToAdd);
                if (targetAdd.isEmpty()) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_NOT_FOUND)));
                    return;
                }
                maintenanceSystemManager.addMaintenance(targetAdd.get().getUniqueId());
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_ADDED, playerToAdd)));
                break;

            case "remove":
                if (args.length < 3) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE)));
                    return;
                }
                String playerToRemove = args[2];
                Optional<Player> targetRemove = plugin.getServer().getPlayer(playerToRemove);
                if (targetRemove.isEmpty()) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_NOT_FOUND)));
                    return;
                }
                maintenanceSystemManager.removeMaintenance(targetRemove.get().getUniqueId());
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_PLAYER_REMOVED, playerToRemove)));
                break;

            case "list":
                String allowedList = maintenanceSystemManager.getMaintenanceList();
                if (allowedList.isEmpty()) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_LIST_EMPTY)));
                    return;
                }
                List<String> playerNames = Arrays.stream(allowedList.split(","))
                    .map(UUID::fromString)
                    .map(uuid -> plugin.getServer().getPlayer(uuid))
                    .filter(Optional::isPresent)
                    .map(player -> player.get().getUsername())
                    .collect(Collectors.toList());
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_LIST, String.join(", ", playerNames))));
                break;

            default:
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.MAINTENANCE_USAGE)));
                break;
        }
    }

    private void handleSettings(CommandSource source, String[] args) {
        if (!source.hasPermission(Permissions.SETTINGS_MANAGE)) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.ADMIN_NO_PERMISSION)));
            return;
        }

        if (args.length < 2) {
            source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_USAGE)));
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "set":
                if (args.length < 4) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_USAGE)));
                    return;
                }
                String settingName = args[2];
                String settingValue = args[3];
                cloudSettings.setSetting(settingName, settingValue);
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_SET, settingName, settingValue)));
                break;

            case "get":
                if (args.length < 3) {
                    source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_USAGE)));
                    return;
                }
                String setting = args[2];
                String value = cloudSettings.getSetting(setting);
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_GET, setting, value)));
                break;

            default:
                source.sendMessage(legacySerializer.deserialize(messageManager.getColoredMessage(Messages.SETTINGS_USAGE)));
                break;
        }
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        List<String> suggestions = new ArrayList<>();
        String[] args = invocation.arguments();

        if (args.length == 1) {
            if (invocation.source().hasPermission(Permissions.ADMIN_RELOAD)) {
                suggestions.add("reload");
            }
            if (invocation.source().hasPermission(Permissions.ADMIN_CLEAR_CACHE)) {
                suggestions.add("clearcache");
            }
            if (invocation.source().hasPermission(Permissions.ADMIN_VERSION)) {
                suggestions.add("version");
            }
            if (invocation.source().hasPermission(Permissions.MAINTENANCE_MANAGE)) {
                suggestions.add("maintenance");
            }
            if (invocation.source().hasPermission(Permissions.SETTINGS_MANAGE)) {
                suggestions.add("settings");
            }
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            switch (subCommand) {
                case "maintenance":
                    if (invocation.source().hasPermission(Permissions.MAINTENANCE_MANAGE)) {
                        suggestions.addAll(Arrays.asList("on", "off", "add", "remove", "list"));
                    }
                    break;
                case "settings":
                    if (invocation.source().hasPermission(Permissions.SETTINGS_MANAGE)) {
                        suggestions.addAll(Arrays.asList("set", "get"));
                    }
                    break;
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            String action = args[1].toLowerCase();
            if (subCommand.equals("maintenance") && (action.equals("add") || action.equals("remove"))) {
                // Suggest online players
                plugin.getServer().getAllPlayers().forEach(player -> suggestions.add(player.getUsername()));
            } else if (subCommand.equals("settings") && action.equals("set")) {
                // Suggest available settings
                suggestions.addAll(Settings.getGlobalSettings());
            }
        }

        return suggestions;
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission(Permissions.ADMIN_VERSION) ||
               invocation.source().hasPermission(Permissions.ADMIN_RELOAD) ||
               invocation.source().hasPermission(Permissions.ADMIN_CLEAR_CACHE) ||
               invocation.source().hasPermission(Permissions.MAINTENANCE_MANAGE) ||
               invocation.source().hasPermission(Permissions.SETTINGS_MANAGE);
    }
} 