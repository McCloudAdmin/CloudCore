package systems.mythical.cloudcore.bungee;

import net.md_5.bungee.api.plugin.Plugin;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.events.ChatEvent;
import systems.mythical.cloudcore.kick.KickExecutorFactory;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.events.QuitEvent;
import systems.mythical.cloudcore.bungee.events.OnConnect;
import systems.mythical.cloudcore.bungee.events.OnServerSwitch;
import systems.mythical.cloudcore.bungee.commands.ProxyConsoleCommand;
import systems.mythical.cloudcore.bungee.events.OnChat;
import systems.mythical.cloudcore.bungee.kick.BungeeKickExecutor;
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.bungee.events.OnQuit;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.permissions.PlatformPermissionBridge;
import systems.mythical.cloudcore.events.CommandEvent;
import systems.mythical.cloudcore.bungee.events.OnCommand;
import systems.mythical.cloudcore.bungee.commands.AlertCommand;
import systems.mythical.cloudcore.bungee.commands.ReportCommand;
import systems.mythical.cloudcore.bungee.commands.Social;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.bungee.commands.CloudCoreCommand;
import systems.mythical.cloudcore.bungee.commands.PanelCommand;
import systems.mythical.cloudcore.utils.DependencyManager;
import systems.mythical.cloudcore.bungee.tasks.ConsoleTaskScheduler;
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.DefaultCloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

import java.util.UUID;
import java.util.logging.Logger;

import systems.mythical.cloudcore.bungee.hooks.LiteBans;
import systems.mythical.cloudcore.bungee.commands.InfoCommand;
 
import systems.mythical.cloudcore.bungee.commands.ProfileCommand;
import systems.mythical.cloudcore.bungee.commands.ChatlogCommand;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import systems.mythical.cloudcore.bungee.events.LuckPermsListener;

public class CloudCoreBungee extends Plugin {
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;
    private final Logger logger = getLogger();
    private ConsoleTaskScheduler consoleTaskScheduler;
    private CloudLogger cloudLogger;

    @SuppressWarnings("unused")
    private boolean litebansEnabled = false;

    @Override
    public void onEnable() {
        cloudLogger = new DefaultCloudLogger(logger);
        cloudLogger.setDebugSupplier(() -> cloudCore != null && cloudCore.getConfig().isDebugMode());
        CloudLoggerFactory.init(cloudLogger);
        cloudLogger.info("CloudCore has been initialized!");
        cloudLogger.info("Forcing plugin to run in production mode.");

        if (getProxy().getPluginManager().getPlugin("LuckPerms") == null) {
            cloudLogger.info("Checking and downloading required dependencies...");
            try {
                DependencyManager dependencyManager = new DependencyManager(
                        logger,
                        getDataFolder().toPath().getParent(),
                        DependencyManager.Platform.BUNGEECORD);

                dependencyManager.checkAndDownloadDependencies()
                        .thenAccept(success -> {
                            if (!success) {
                                cloudLogger.error("Failed to download required dependencies");
                                getProxy().stop();
                            } else {
                                cloudLogger.info("Successfully downloaded all required dependencies");
                                getProxy().stop(); // Restart to load new plugins
                            }
                        })
                        .exceptionally(ex -> {
                            cloudLogger.error("Error managing dependencies: " + ex.getMessage());
                            ex.printStackTrace();
                            getProxy().stop();
                            return null;
                        });
                return;
            } catch (Exception e) {
                cloudLogger.error("Failed to initialize dependency manager: " + e.getMessage());
                e.printStackTrace();
                getProxy().stop();
                return;
            }
        }

        KickExecutorFactory.setExecutor(BungeeKickExecutor.getInstance());

        if (getProxy().getPluginManager().getPlugin("LiteBans") == null) {
            litebansEnabled = false;
            cloudLogger.info("LiteBans is not installed, LiteBans support is disabled.");
        } else {
            litebansEnabled = true;
            cloudLogger.info("LiteBans is installed, LiteBans support is enabled.");
            cloudLogger.info("CloudCore will process bans from LiteBans to panel.");
            LiteBans liteBans = new LiteBans(this);
            liteBans.registerEvents();
        }

        // Set up permission checker
        PlatformPermissionBridge.setPermissionChecker((UUID uuid, String permission) -> {
            var player = getProxy().getPlayer(uuid);
            return player != null && player.hasPermission(permission);
        });

        try {
            // Initialize CloudCore
            cloudCore = new CloudCore(getDataFolder(), logger, false);

            // Initialize database
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);
            cloudLogger.info("Database connection pool initialized successfully!");

            // Initialize system user
            systems.mythical.cloudcore.users.SystemUserManager systemUserManager = 
                systems.mythical.cloudcore.users.SystemUserManager.getInstance(databaseManager, logger);
            systemUserManager.createSystemUserIfNotExists();
            cloudLogger.info("System user initialized successfully!");

            // Initialize worker
            WorkerManager worker = new WorkerManager(cloudCore.getConfig(), databaseManager, logger);
            worker.initialize();
            cloudLogger.info("Worker initialized successfully!");

            // Initialize settings
            CloudSettings cloudSettings = CloudSettings.getInstance(databaseManager, logger);
            SettingsManager settingsManager = SettingsManager.getInstance(cloudSettings, logger);

            // Initialize events based on settings
            CommonSettings.BooleanSetting logChatEvents = new CommonSettings.BooleanSetting(Settings.LOG_CHAT, true);
            CommonSettings.BooleanSetting logCommandEvents = new CommonSettings.BooleanSetting(Settings.LOG_COMMANDS,
                    true);

            JoinEvent.initialize(databaseManager);
            cloudLogger.info("[CloudCore] Join events initialized");

            ServerSwitchEvent.initialize(databaseManager);
            cloudLogger.info("[CloudCore] Server switch events initialized");

            QuitEvent.initialize(databaseManager);
            cloudLogger.info("[CloudCore] Quit events initialized");

            if (settingsManager.getValue(logChatEvents)) {
                ChatEvent.initialize(databaseManager);
                cloudLogger.info("[CloudCore] Chat events initialized");
            }

            if (settingsManager.getValue(logCommandEvents)) {
                CommandEvent.initialize(databaseManager);
                cloudLogger.info("[CloudCore] Command events initialized");
                ConsoleCommand.initialize(databaseManager, getLogger());
                cloudLogger.info("[CloudCore] Console commands initialized");
            }

            MaintenanceSystemCommand.initialize(databaseManager, logger);

            // Register events only if they are enabled
            getProxy().getPluginManager().registerListener(this, new OnConnect(databaseManager, logger));
            getProxy().getPluginManager().registerListener(this, new OnServerSwitch());
            getProxy().getPluginManager().registerListener(this, new OnQuit());
            if (settingsManager.getValue(logChatEvents)) {
                getProxy().getPluginManager().registerListener(this, new OnChat(cloudLogger));
            }
            if (settingsManager.getValue(logCommandEvents)) {
                getProxy().getPluginManager().registerListener(this, new OnCommand(cloudLogger));
            }

            // Initialize commands
            initializeCommands(settingsManager);
            createSocialMediaLinksCommands(cloudSettings);

            // Initialize console task scheduler
            consoleTaskScheduler = new ConsoleTaskScheduler(this);
            consoleTaskScheduler.start();

            // Initialize LuckPerms listener
            LuckPerms luckPerms = LuckPermsProvider.get();
            new LuckPermsListener(luckPerms, databaseManager, logger);
            cloudLogger.info("[CloudCore] LuckPerms listener initialized");

            cloudLogger.info("CloudCore BungeeCord plugin has been enabled!");
        } catch (Exception e) {
            cloudLogger.error("Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        // Mark all users as offline before shutdown
        if (databaseManager != null) {
            UserManager userManager = UserManager.getInstance(databaseManager, logger);
            userManager.markAllUsersOffline();
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }

        // Stop console task scheduler
        if (consoleTaskScheduler != null) {
            consoleTaskScheduler.stop();
        }

        cloudLogger.info("CloudCore BungeeCord plugin has been disabled!");
    }

    public void initializeCommands(SettingsManager settingsManager) {
        // Register CloudCore command
        getProxy().getPluginManager().registerCommand(this, new CloudCoreCommand(this));

        // Register Panel command
        getProxy().getPluginManager().registerCommand(this, new PanelCommand(this));

        // Register Info command
        getProxy().getPluginManager().registerCommand(this, new InfoCommand(this));

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.GLOBAL_APP_PROFILE_ENABLED, false))) {
            // Register Profile command
            getProxy().getPluginManager().registerCommand(this, new ProfileCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false))) {
            // Register Chatlog command
            getProxy().getPluginManager().registerCommand(this, new ChatlogCommand(this));
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_ALERT_COMMAND, false))) {
            // Register alert command
            getProxy().getPluginManager().registerCommand(this, new AlertCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.REPORT_SYSTEM_ENABLED, false))) {
            // Register report command
            getProxy().getPluginManager().registerCommand(this, new ReportCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_CONSOLE_COMMAND, false))) {
            // Register proxy console command
            getProxy().getPluginManager().registerCommand(this, new ProxyConsoleCommand(this));
        }
        

    }

    public void createSocialMediaLinksCommands(CloudSettings cloudSettings) {
        if (cloudSettings.getSetting(Settings.GLOBAL_TWITTER_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("twitter", cloudSettings.getSetting(Settings.GLOBAL_TWITTER_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_DISCORD_INVITE_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("discord", cloudSettings.getSetting(Settings.GLOBAL_DISCORD_INVITE_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_GITHUB_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("github", cloudSettings.getSetting(Settings.GLOBAL_GITHUB_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_YOUTUBE_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("youtube", cloudSettings.getSetting(Settings.GLOBAL_YOUTUBE_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_TIKTOK_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("tiktok", cloudSettings.getSetting(Settings.GLOBAL_TIKTOK_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_FACEBOOK_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("facebook", cloudSettings.getSetting(Settings.GLOBAL_FACEBOOK_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_REDDIT_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("reddit", cloudSettings.getSetting(Settings.GLOBAL_REDDIT_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_TELEGRAM_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("telegram", cloudSettings.getSetting(Settings.GLOBAL_TELEGRAM_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_WHATSAPP_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("whatsapp", cloudSettings.getSetting(Settings.GLOBAL_WHATSAPP_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_INSTAGRAM_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("instagram", cloudSettings.getSetting(Settings.GLOBAL_INSTAGRAM_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_WEBSITE_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("website", cloudSettings.getSetting(Settings.GLOBAL_WEBSITE_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_STORE_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("store", cloudSettings.getSetting(Settings.GLOBAL_STORE_URL), this));
        }
        if (cloudSettings.getSetting(Settings.GLOBAL_STATUS_PAGE_URL) != "") {
            getProxy().getPluginManager().registerCommand(this,
                    new Social("status", cloudSettings.getSetting(Settings.GLOBAL_STATUS_PAGE_URL), this));
        }
    }

    /**
     * Gets the database manager
     * 
     * @author MythicalSystems
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Gets the CloudCore instance
     * 
     * @author MythicalSystems
     * @return The CloudCore instance
     */
    public CloudCore getCloudCore() {
        return cloudCore;
    }
}