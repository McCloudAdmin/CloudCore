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
import systems.mythical.cloudcore.velocity.commands.ProfileCommand;
import systems.mythical.cloudcore.velocity.commands.InfoCommand;
import systems.mythical.cloudcore.velocity.commands.ChatlogCommand;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.utils.DependencyManager;
import systems.mythical.cloudcore.velocity.tasks.ConsoleTaskScheduler;
import systems.mythical.cloudcore.velocity.commands.Social;
 

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
import systems.mythical.cloudcore.utils.CloudLogger;
import systems.mythical.cloudcore.utils.DefaultCloudLogger;
import systems.mythical.cloudcore.utils.CloudLoggerFactory;

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
    private CloudLogger cloudLogger;

    @SuppressWarnings("unused")
    private boolean litebansEnabled = false;

    @Inject
    public CloudCoreVelocity(ProxyServer server, Logger logger, @DataDirectory Path dataFolder) {
        this.server = server;
        this.logger = logger;
        this.dataFolder = dataFolder;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        cloudLogger = new DefaultCloudLogger(logger);
        cloudLogger.setDebugSupplier(() -> cloudCore != null && cloudCore.getConfig().isDebugMode());
        CloudLoggerFactory.init(cloudLogger);
        cloudLogger.info("[CloudCore] onProxyInitialization called.");
        cloudLogger.info("CloudCore has been initialized!");
        cloudLogger.info("Forcing plugin to run in production mode.");

        if (!server.getPluginManager().getPlugin("luckperms").isPresent()) {
            cloudLogger.info("Checking and downloading required dependencies...");
            try {
                DependencyManager dependencyManager = new DependencyManager(
                        logger,
                        dataFolder.getParent(),
                        DependencyManager.Platform.VELOCITY);

                dependencyManager.checkAndDownloadDependencies()
                        .thenAccept(success -> {
                            if (!success) {
                                cloudLogger.error("Failed to download required dependencies");
                                server.shutdown();
                            } else {
                                cloudLogger.info("Successfully downloaded all required dependencies");
                                server.shutdown(); // Restart to load new plugins
                            }
                        })
                        .exceptionally(ex -> {
                            cloudLogger.error("Error managing dependencies: " + ex.getMessage());
                            ex.printStackTrace();
                            server.shutdown();
                            return null;
                        });
                return;
            } catch (Exception e) {
                cloudLogger.error("Failed to initialize dependency manager: " + e.getMessage());
                e.printStackTrace();
                server.shutdown();
                return;
            }
        }

        if (!server.getPluginManager().getPlugin("litebans").isPresent()) {
            litebansEnabled = false;
            cloudLogger.info("LiteBans is not installed, LiteBans support is disabled.");
        } else {
            litebansEnabled = true;
            cloudLogger.info("LiteBans is installed, LiteBans support is enabled.");
            cloudLogger.info("CloudCore will process bans from LiteBans to panel.");
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
            cloudLogger.info("[CloudCore] Initializing CloudCore...");
            cloudCore = new CloudCore(dataFolder.toFile(), logger, false);

            cloudLogger.info("[CloudCore] Initializing database...");
            databaseManager = new DatabaseManager(cloudCore.getConfig(), logger);

            // Initialize system user
            SystemUserManager systemUserManager = SystemUserManager.getInstance(databaseManager, logger);
            systemUserManager.createSystemUserIfNotExists();
            cloudLogger.info("[CloudCore] System user initialized successfully!");

            // Initialize settings
            CloudSettings cloudSettings = CloudSettings.getInstance(databaseManager, logger);
            SettingsManager settingsManager = SettingsManager.getInstance(cloudSettings, logger);

            // Initialize events based on settings
            CommonSettings.BooleanSetting logChatEvents = new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false);
            CommonSettings.BooleanSetting logCommandEvents = new CommonSettings.BooleanSetting(Settings.LOG_COMMANDS,
                    false);

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
                ConsoleCommand.initialize(databaseManager, logger);
                cloudLogger.info("[CloudCore] Console commands initialized");
            }

            MaintenanceSystemCommand.initialize(databaseManager, logger);

            // Register events
            server.getEventManager().register(this, new OnConnect(databaseManager, logger));
            server.getEventManager().register(this, new OnServerSwitch());
            server.getEventManager().register(this, new OnQuit());

            if (settingsManager.getValue(logChatEvents)) {
                server.getEventManager().register(this, new OnChat(cloudLogger));
            }
            if (settingsManager.getValue(logCommandEvents)) {
                server.getEventManager().register(this, new OnCommand(cloudLogger));
            }

            // Initialize LuckPerms listener
            LuckPerms luckPerms = LuckPermsProvider.get();
            new LuckPermsListener(luckPerms, databaseManager, logger);
            cloudLogger.info("[CloudCore] LuckPerms listener initialized");

            cloudLogger.info("[CloudCore] Initializing commands...");
            initializeCommands(cloudSettings, settingsManager);

            // Initialize console task scheduler
            consoleTaskScheduler = new ConsoleTaskScheduler(this);
            consoleTaskScheduler.start();

            // Initialize worker
            WorkerManager worker = new WorkerManager(cloudCore.getConfig(), databaseManager, logger);
            worker.initialize();
            cloudLogger.info("[CloudCore] Worker initialized successfully!");

            cloudLogger.info("[CloudCore] CloudCore Velocity plugin has been enabled!");
        } catch (Exception e) {
            cloudLogger.error("[CloudCore] Failed to initialize CloudCore: " + e.getMessage());
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

        cloudLogger.info("CloudCore Velocity plugin has been disabled!");
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
        cloudLogger.info("[CloudCore] Registering commands...");
        CommandManager commandManager = server.getCommandManager();

        // Register CloudCore command
        try {
            SimpleCommand cloudCore = new CloudCoreCommand(this);
            CommandMeta mainMeta = commandManager.metaBuilder("cloudcore")
                    .aliases("cc")
                    .plugin(this)
                    .build();
            commandManager.register(mainMeta, cloudCore);
            cloudLogger.info("[CloudCore] /cloudcore command registered successfully.");
        } catch (Exception e) {
            cloudLogger.error("[CloudCore] Failed to register /cloudcore: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Panel command
        try {
            SimpleCommand panel = new PanelCommand(this);
            CommandMeta panelMeta = commandManager.metaBuilder("panel")
                    .plugin(this)
                    .build();
            commandManager.register(panelMeta, panel);
            cloudLogger.info("[CloudCore] /panel command registered successfully.");
        } catch (Exception e) {
            cloudLogger.error("[CloudCore] Failed to register /panel: " + e.getMessage());
            e.printStackTrace();
        }

        // Register Info command
        try {
            SimpleCommand info = new InfoCommand(this);
            CommandMeta infoMeta = commandManager.metaBuilder("info")
                    .plugin(this)
                    .build();
            commandManager.register(infoMeta, info);
            cloudLogger.info("[CloudCore] /info command registered successfully.");
        } catch (Exception e) {
            cloudLogger.error("[CloudCore] Failed to register /info: " + e.getMessage());
            e.printStackTrace();
        }

        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.ENABLE_ALERT_COMMAND, false))) {
            try {
                SimpleCommand alert = new AlertCommand(this);
                CommandMeta alertMeta = commandManager.metaBuilder("alert")
                        .plugin(this)
                        .build();
                commandManager.register(alertMeta, alert);
                cloudLogger.info("[CloudCore] /alert command registered successfully.");
            } catch (Exception e) {
                cloudLogger.error("[CloudCore] Failed to register /alert: " + e.getMessage());
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
                cloudLogger.info("[CloudCore] /report command registered successfully.");
            } catch (Exception e) {
                cloudLogger.error("[CloudCore] Failed to register /report: " + e.getMessage());
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
                cloudLogger.info("[CloudCore] /proxyconsole command registered successfully.");
            } catch (Exception e) {
                cloudLogger.error("[CloudCore] Failed to register /proxyconsole: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Register commands (avoid deprecated register(String, Command,...))
        {
            CommandMeta infoMeta = commandManager.metaBuilder("info").plugin(this).build();
            commandManager.register(infoMeta, new InfoCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.GLOBAL_APP_PROFILE_ENABLED, false))) {
            CommandMeta profileMeta = commandManager.metaBuilder("profile").plugin(this).build();
            commandManager.register(profileMeta, new ProfileCommand(this));
        }
        if (settingsManager.getValue(new CommonSettings.BooleanSetting(Settings.LOG_CHAT, false))) {
            CommandMeta chatlogMeta = commandManager.metaBuilder("chatlog").plugin(this).build();
            commandManager.register(chatlogMeta, new ChatlogCommand(this));
        }
        {
            CommandMeta panelMeta = commandManager.metaBuilder("panel").plugin(this).build();
            commandManager.register(panelMeta, new PanelCommand(this));
        }

        createSocialMediaLinksCommands(commandManager, cloudSettings);
    }

    private void createSocialMediaLinksCommands(CommandManager commandManager, CloudSettings cloudSettings) {
        String discordUrl = cloudSettings.getSetting(Settings.GLOBAL_DISCORD_INVITE_URL);
        if (!discordUrl.isEmpty()) {
            CommandMeta discordMeta = commandManager.metaBuilder("discord").plugin(this).build();
            commandManager.register(discordMeta, new Social("Discord", discordUrl, this));
        }

        String websiteUrl = cloudSettings.getSetting(Settings.GLOBAL_WEBSITE_URL);
        if (!websiteUrl.isEmpty()) {
            CommandMeta websiteMeta = commandManager.metaBuilder("website").plugin(this).build();
            commandManager.register(websiteMeta, new Social("Website", websiteUrl, this));
        }

        String storeUrl = cloudSettings.getSetting(Settings.GLOBAL_STORE_URL);
        if (!storeUrl.isEmpty()) {
            CommandMeta storeMeta = commandManager.metaBuilder("store").plugin(this).build();
            commandManager.register(storeMeta, new Social("Store", storeUrl, this));
        }

        String twitterUrl = cloudSettings.getSetting(Settings.GLOBAL_TWITTER_URL);
        if (!twitterUrl.isEmpty()) {
            CommandMeta twitterMeta = commandManager.metaBuilder("twitter").plugin(this).build();
            commandManager.register(twitterMeta, new Social("Twitter", twitterUrl, this));
        }

        String githubUrl = cloudSettings.getSetting(Settings.GLOBAL_GITHUB_URL);
        if (!githubUrl.isEmpty()) {
            CommandMeta githubMeta = commandManager.metaBuilder("github").plugin(this).build();
            commandManager.register(githubMeta, new Social("GitHub", githubUrl, this));
        }

        String linkedinUrl = cloudSettings.getSetting(Settings.GLOBAL_LINKEDIN_URL);
        if (!linkedinUrl.isEmpty()) {
            CommandMeta linkedinMeta = commandManager.metaBuilder("linkedin").plugin(this).build();
            commandManager.register(linkedinMeta, new Social("LinkedIn", linkedinUrl, this));
        }

        String instagramUrl = cloudSettings.getSetting(Settings.GLOBAL_INSTAGRAM_URL);
        if (!instagramUrl.isEmpty()) {
            CommandMeta instagramMeta = commandManager.metaBuilder("instagram").plugin(this).build();
            commandManager.register(instagramMeta, new Social("Instagram", instagramUrl, this));
        }

        String youtubeUrl = cloudSettings.getSetting(Settings.GLOBAL_YOUTUBE_URL);
        if (!youtubeUrl.isEmpty()) {
            CommandMeta youtubeMeta = commandManager.metaBuilder("youtube").plugin(this).build();
            commandManager.register(youtubeMeta, new Social("YouTube", youtubeUrl, this));
        }

        String tiktokUrl = cloudSettings.getSetting(Settings.GLOBAL_TIKTOK_URL);
        if (!tiktokUrl.isEmpty()) {
            CommandMeta tiktokMeta = commandManager.metaBuilder("tiktok").plugin(this).build();
            commandManager.register(tiktokMeta, new Social("TikTok", tiktokUrl, this));
        }

        String facebookUrl = cloudSettings.getSetting(Settings.GLOBAL_FACEBOOK_URL);
        if (!facebookUrl.isEmpty()) {
            CommandMeta facebookMeta = commandManager.metaBuilder("facebook").plugin(this).build();
            commandManager.register(facebookMeta, new Social("Facebook", facebookUrl, this));
        }

        String redditUrl = cloudSettings.getSetting(Settings.GLOBAL_REDDIT_URL);
        if (!redditUrl.isEmpty()) {
            CommandMeta redditMeta = commandManager.metaBuilder("reddit").plugin(this).build();
            commandManager.register(redditMeta, new Social("Reddit", redditUrl, this));
        }

        String telegramUrl = cloudSettings.getSetting(Settings.GLOBAL_TELEGRAM_URL);
        if (!telegramUrl.isEmpty()) {
            CommandMeta telegramMeta = commandManager.metaBuilder("telegram").plugin(this).build();
            commandManager.register(telegramMeta, new Social("Telegram", telegramUrl, this));
        }

        String whatsappUrl = cloudSettings.getSetting(Settings.GLOBAL_WHATSAPP_URL);
        if (!whatsappUrl.isEmpty()) {
            CommandMeta whatsappMeta = commandManager.metaBuilder("whatsapp").plugin(this).build();
            commandManager.register(whatsappMeta, new Social("WhatsApp", whatsappUrl, this));
        }

        String statusUrl = cloudSettings.getSetting(Settings.GLOBAL_STATUS_PAGE_URL);
        if (!statusUrl.isEmpty()) {
            CommandMeta statusMeta = commandManager.metaBuilder("status").plugin(this).build();
            commandManager.register(statusMeta, new Social("Status", statusUrl, this));
        }
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public CloudCore getCloudCore() {
        return cloudCore;
    }
}