package systems.mythical.cloudcore.velocity;

/**
 * Google Imports
 */
import com.google.inject.Inject;
/**
 * CloudCore Imports
 */
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.events.JoinEvent;
import systems.mythical.cloudcore.events.QuitEvent;
import systems.mythical.cloudcore.events.ServerSwitchEvent;
import systems.mythical.cloudcore.events.ChatEvent;
import systems.mythical.cloudcore.events.CommandEvent;
import systems.mythical.cloudcore.kick.KickExecutorFactory;
import systems.mythical.cloudcore.maintenance.MaintenanceSystemCommand;
import systems.mythical.cloudcore.velocity.events.OnConnect;
import systems.mythical.cloudcore.velocity.events.OnQuit;
import systems.mythical.cloudcore.velocity.events.OnServerSwitch;
import systems.mythical.cloudcore.velocity.hooks.LiteBans;
import systems.mythical.cloudcore.velocity.events.OnChat;
import systems.mythical.cloudcore.velocity.events.OnCommand;
import systems.mythical.cloudcore.velocity.kick.VelocityKickExecutor;
import systems.mythical.cloudcore.users.SystemUserManager;
import systems.mythical.cloudcore.users.UserManager;
import systems.mythical.cloudcore.permissions.PlatformPermissionBridge;
import systems.mythical.cloudcore.velocity.commands.ProxyConsoleCommand;
import systems.mythical.cloudcore.velocity.commands.AlertCommand;
import systems.mythical.cloudcore.velocity.commands.ReportCommand;
import systems.mythical.cloudcore.velocity.commands.CloudCoreCommand;
import systems.mythical.cloudcore.velocity.commands.PanelCommand;
import systems.mythical.cloudcore.velocity.commands.PerformJoinCommand;
import systems.mythical.cloudcore.velocity.commands.ProfileCommand;
import systems.mythical.cloudcore.velocity.commands.InfoCommand;
import systems.mythical.cloudcore.velocity.commands.ChatlogCommand;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.utils.DependencyManager;
import systems.mythical.cloudcore.velocity.tasks.ConsoleTaskScheduler;
import systems.mythical.cloudcore.velocity.commands.Social;
import systems.mythical.cloudcore.velocity.commands.JoinMeCommand;

/**
 * Velocity Imports
 */
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.plugin.Dependency;

/*
 * Java Imports
 */
import java.nio.file.Path;
import java.util.UUID;
import java.util.logging.Logger;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import systems.mythical.cloudcore.velocity.events.LuckPermsListener;
import systems.mythical.cloudcore.velocity.worker.WorkerManager;

@Plugin(id = "cloudcore", name = "CloudCore", version = "1.0-SNAPSHOT", description = "The plugin to run McCloudAdminPanel", authors = {
        "MythicalSystems" }, dependencies = {
                @Dependency(id = "luckperms", optional = true),
                @Dependency(id = "litebans", optional = true)
        })
public class CloudCoreVelocity {
    private final ProxyServer server;
    private final Logger logger;
    private final Path dataFolder;
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;
    private ConsoleTaskScheduler consoleTaskScheduler;

    @SuppressWarnings("unused")
    private boolean litebansEnabled = false;

    @Inject
    public CloudCoreVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("[CloudCore] onProxyInitialization called.");
        logger.info("CloudCore has been initialized!");
        logger.info("Forcing plugin to run in production mode.");

        if (!server.getPluginManager().getPlugin("luckperms").isPresent()) {
            logger.info("Checking and downloading required dependencies...");
            try {
                DependencyManager dependencyManager = new DependencyManager(
                        logger,
                        dataFolder.getParent(),
                        DependencyManager.Platform.VELOCITY);

                dependencyManager.checkAndDownloadDependencies()
                        .thenAccept(success -> {
                            if (!success) {
                                logger.severe("Failed to download required dependencies");
                                server.shutdown();
                            } else {
                                logger.info("Successfully downloaded all required dependencies");
                                server.shutdown(); // Restart to load new plugins
                            }
                        })
                        .exceptionally(ex -> {
                            logger.severe("Error managing dependencies: " + ex.getMessage());
                            ex.printStackTrace();
                            server.shutdown();
                            return null;
                        });
                return;
            } catch (Exception e) {
                logger.severe("Failed to initialize dependency manager: " + e.getMessage());
                e.printStackTrace();
                server.shutdown();
                return;
            }
        }

        if (!server.getPluginManager().getPlugin("litebans").isPresent()) {
            litebansEnabled = false;
            logger.info("LiteBans is not installed, LiteBans support is disabled.");
        } else {
            litebansEnabled = true;
            logger.info("LiteBans is installed, LiteBans support is enabled.");
            logger.info("CloudCore will process bans from LiteBans to panel.");
            LiteBans liteBans = new LiteBans(this);
            liteBans.registerEvents();
        }

        KickExecutorFactory.setExecutor(VelocityKickExecutor.getInstance(server));

        // Set up permission checker
        PlatformPermissionBridge.setPermissionChecker((UUID uuid, String permission) -> {
            var player = server.getPlayer(uuid);
            return player.isPresent() && player.get().hasPermission(permission);
        });

        try {
            logger.info("[CloudCore] Initializing CloudCore...");
            cloudCore = new CloudCore(dataFolder.toFile(), logger, false);

            logger.info("[CloudCore] Initializing database...");
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);

            // Initialize system user
            SystemUserManager systemUserManager = SystemUserManager.getInstance(databaseManager, logger);
            systemUserManager.createSystemUserIfNotExists();
            logger.info("[CloudCore] System user initialized successfully!");

            // Initialize settings
            CloudSettings cloudSettings = CloudSettings.getInstance(databaseManager, logger);
            SettingsManager settingsManager = SettingsManager.getInstance(cloudSettings, logger);

            // Initialize events based on settings
            CommonSettings.BooleanSetting logChatEvents = new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false);
            CommonSettings.BooleanSetting logCommandEvents = new CommonSettings.BooleanSetting(Settings.LOG_COMMANDS,
                    false);

            JoinEvent.initialize(databaseManager);
            logger.info("[CloudCore] Join events initialized");

            ServerSwitchEvent.initialize(databaseManager);
            logger.info("[CloudCore] Server switch events initialized");

            QuitEvent.initialize(databaseManager);
            logger.info("[CloudCore] Quit events initialized");

            if (settingsManager.getValue(logChatEvents)) {
                ChatEvent.initialize(databaseManager);
                logger.info("[CloudCore] Chat events initialized");
            }

            if (settingsManager.getValue(logCommandEvents)) {
                CommandEvent.initialize(databaseManager);
                logger.info("[CloudCore] Command events initialized");
                ConsoleCommand.initialize(databaseManager);
                logger.info("[CloudCore] Console commands initialized");
            }

            MaintenanceSystemCommand.initialize(databaseManager, logger);

            // Register events
            server.getEventManager().register(this, new OnConnect(databaseManager, logger));
            server.getEventManager().register(this, new OnServerSwitch());
            server.getEventManager().register(this, new OnQuit());

            if (settingsManager.getValue(logChatEvents)) {
                server.getEventManager().register(this, new OnChat());
            }
            if (settingsManager.getValue(logCommandEvents)) {
                server.getEventManager().register(this, new OnCommand());
            }

            // Initialize LuckPerms listener
            LuckPerms luckPerms = LuckPermsProvider.get();
            new LuckPermsListener(luckPerms, databaseManager, logger);
            logger.info("[CloudCore] LuckPerms listener initialized");

            logger.info("[CloudCore] Initializing commands...");
            initializeCommands(cloudSettings, settingsManager);

            // Initialize console task scheduler
            consoleTaskScheduler = new ConsoleTaskScheduler(this);
            consoleTaskScheduler.start();

            // Initialize worker
            WorkerManager worker = new WorkerManager(cloudCore.getConfig(), databaseManager, logger);
            worker.initialize();
            logger.info("[CloudCore] Worker initialized successfully!");

            logger.info("[CloudCore] CloudCore Velocity plugin has been enabled!");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to initialize CloudCore: " + e.getMessage());
            e.printStackTrace();
        }
    }

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

        logger.info("CloudCore Velocity plugin has been disabled!");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        onDisable();
    }

    public Logger getLogger() {
        return logger;
    }

    public ProxyServer getServer() {
        return server;
    }

    public void initializeCommands(CloudSettings cloudSettings, SettingsManager settingsManager) {
        logger.info("[CloudCore] Registering commands...");
        CommandManager commandManager = server.getCommandManager();

        // Register CloudCore command
        try {
            SimpleCommand cloudCore = new CloudCoreCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("cloudcore")
                    .aliases("cc")
                    .plugin(this)
                    .build();
            commandManager.register(mainMeta, cloudCore);
            logger.info("[CloudCore] /cloudcore command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /cloudcore: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Panel command
        try {
            SimpleCommand panel = new PanelCommand(this);
            CommandMeta panelMeta = commandManager.metaBuilder("panel")
                    .plugin(this)
                    .build();
            commandManager.register(panelMeta, panel);
            logger.info("[CloudCore] /panel command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /panel: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Info command
        try {
            SimpleCommand info = new InfoCommand(this);
            CommandMeta infoMeta = commandManager.metaBuilder("info")
                    .plugin(this)
                    .build();
            commandManager.register(infoMeta, info);
            logger.info("[CloudCore] /info command registered successfully.");
        } catch (Exception e) {
            logger.severe("[CloudCore] Failed to register /info: " + e.getMessage());
            e.printStackTrace();
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_ALERT_COMMAND, false))) {
            try {
                SimpleCommand alert = new AlertCommand(this);
                CommandMeta alertMeta = commandManager.metaBuilder("alert")
                        .plugin(this)
                        .build();
                commandManager.register(alertMeta, alert);
                logger.info("[CloudCore] /alert command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /alert: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.JOINME_ENABLED, false))) {
            try {
                // Create the JoinMe command instance
                JoinMeCommand joinMeCommand = new JoinMeCommand(this, dataFolder);

                // Register the main joinme command
                CommandMeta joinMeMeta = commandManager.metaBuilder("joinme")
                        .plugin(this)
                        .build();
                commandManager.register(joinMeMeta, joinMeCommand);

                // Register the performjoin command
                PerformJoinCommand performJoinCommand = new PerformJoinCommand(this);
                CommandMeta performJoinMeta = commandManager.metaBuilder("performjoin")
                        .plugin(this)
                        .aliases("pj") // Optional alias
                        .build();
                commandManager.register(performJoinMeta, performJoinCommand);

                logger.info("[CloudCore] /joinme and /performjoin commands registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register JoinMe commands: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.REPORT_SYSTEM_ENABLED, false))) {
            try {
                SimpleCommand report = new ReportCommand(this);
                CommandMeta reportMeta = commandManager.metaBuilder("report")
                        .plugin(this)
                        .build();
                commandManager.register(reportMeta, report);
                logger.info("[CloudCore] /report command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /report: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_CONSOLE_COMMAND, false))) {
            try {
                SimpleCommand proxyConsole = new ProxyConsoleCommand(this);
                CommandMeta proxyConsoleMeta = commandManager.metaBuilder("proxyconsole")
                        .aliases("pconsole", "pcon")
                        .plugin(this)
                        .build();
                commandManager.register(proxyConsoleMeta, proxyConsole);
                logger.info("[CloudCore] /proxyconsole command registered successfully.");
            } catch (Exception e) {
                logger.severe("[CloudCore] Failed to register /proxyconsole: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Register commands
        commandManager.register("info", new InfoCommand(this));
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.GLOBAL_APP_PROFILE_ENABLED, false))) {
            commandManager.register("profile", new ProfileCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false))) {
            commandManager.register("chatlog", new ChatlogCommand(this));
        }
        commandManager.register("panel", new PanelCommand(this));

        createSocialMediaLinksCommands(commandManager, cloudSettings);
    }

    private void createSocialMediaLinksCommands(CommandManager commandManager, CloudSettings cloudSettings) {
        String discordUrl = cloudSettings.getSetting(Settings.GLOBAL_DISCORD_INVITE_URL);
        if (!discordUrl.isEmpty()) {
            commandManager.register("discord", new Social("Discord", discordUrl, this));
        }

        String websiteUrl = cloudSettings.getSetting(Settings.GLOBAL_WEBSITE_URL);
        if (!websiteUrl.isEmpty()) {
            commandManager.register("website", new Social("Website", websiteUrl, this));
        }

        String storeUrl = cloudSettings.getSetting(Settings.GLOBAL_STORE_URL);
        if (!storeUrl.isEmpty()) {
            commandManager.register("store", new Social("Store", storeUrl, this));
        }

        String twitterUrl = cloudSettings.getSetting(Settings.GLOBAL_TWITTER_URL);
        if (!twitterUrl.isEmpty()) {
            commandManager.register("twitter", new Social("Twitter", twitterUrl, this));
        }

        String githubUrl = cloudSettings.getSetting(Settings.GLOBAL_GITHUB_URL);
        if (!githubUrl.isEmpty()) {
            commandManager.register("github", new Social("GitHub", githubUrl, this));
        }

        String linkedinUrl = cloudSettings.getSetting(Settings.GLOBAL_LINKEDIN_URL);
        if (!linkedinUrl.isEmpty()) {
            commandManager.register("linkedin", new Social("LinkedIn", linkedinUrl, this));
        }

        String instagramUrl = cloudSettings.getSetting(Settings.GLOBAL_INSTAGRAM_URL);
        if (!instagramUrl.isEmpty()) {
            commandManager.register("instagram", new Social("Instagram", instagramUrl, this));
        }

        String youtubeUrl = cloudSettings.getSetting(Settings.GLOBAL_YOUTUBE_URL);
        if (!youtubeUrl.isEmpty()) {
            commandManager.register("youtube", new Social("YouTube", youtubeUrl, this));
        }

        String tiktokUrl = cloudSettings.getSetting(Settings.GLOBAL_TIKTOK_URL);
        if (!tiktokUrl.isEmpty()) {
            commandManager.register("tiktok", new Social("TikTok", tiktokUrl, this));
        }

        String facebookUrl = cloudSettings.getSetting(Settings.GLOBAL_FACEBOOK_URL);
        if (!facebookUrl.isEmpty()) {
            commandManager.register("facebook", new Social("Facebook", facebookUrl, this));
        }

        String redditUrl = cloudSettings.getSetting(Settings.GLOBAL_REDDIT_URL);
        if (!redditUrl.isEmpty()) {
            commandManager.register("reddit", new Social("Reddit", redditUrl, this));
        }

        String telegramUrl = cloudSettings.getSetting(Settings.GLOBAL_TELEGRAM_URL);
        if (!telegramUrl.isEmpty()) {
            commandManager.register("telegram", new Social("Telegram", telegramUrl, this));
        }

        String whatsappUrl = cloudSettings.getSetting(Settings.GLOBAL_WHATSAPP_URL);
        if (!whatsappUrl.isEmpty()) {
            commandManager.register("whatsapp", new Social("WhatsApp", whatsappUrl, this));
        }

        String statusUrl = cloudSettings.getSetting(Settings.GLOBAL_STATUS_PAGE_URL);
        if (!statusUrl.isEmpty()) {
            commandManager.register("status", new Social("Status", statusUrl, this));
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CloudCore getCloudCore() {
        return cloudCore;
    }
}