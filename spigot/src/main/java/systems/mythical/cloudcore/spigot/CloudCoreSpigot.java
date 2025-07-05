package systems.mythical.cloudcore.spigot;
/**
 * Bukkit Imports
 */
import org.bukkit.plugin.java.JavaPlugin;
/*
 * CloudCore Imports
 */
import systems.mythical.cloudcore.console.ConsoleCommand;
import systems.mythical.cloudcore.core.CloudCore;
import systems.mythical.cloudcore.database.DatabaseManager;
import systems.mythical.cloudcore.database.StatsManager;
import systems.mythical.cloudcore.spigot.worker.WorkerManager;
import systems.mythical.cloudcore.spigot.tasks.ConsoleTaskScheduler;
import systems.mythical.cloudcore.spigot.commands.SpigotConsoleCommand;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnMobKill;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerDamageTake;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerDeath;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerJoin;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerKill;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerLeave;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerMineBlock;
import systems.mythical.cloudcore.spigot.events.stats.StatsOnPlayerPlaceBlock;
import systems.mythical.cloudcore.spigot.hooks.DependencyManager;
import systems.mythical.cloudcore.messages.MessageManager;
import systems.mythical.cloudcore.settings.CloudSettings;
import systems.mythical.cloudcore.settings.SettingsManager;
import systems.mythical.cloudcore.settings.CommonSettings;
import systems.mythical.cloudcore.core.CloudCoreConstants.Settings;
import systems.mythical.cloudcore.spigot.stats.StatsBufferManager;

/**
 * Plugin Dependencies
 */
import com.earth2me.essentials.Essentials;
import com.gamingmesh.jobs.Jobs;
import com.wasteofplastic.askyblock.ASkyBlockAPI;

/**
 * CloudCore Spigot Hooks
 */
import systems.mythical.cloudcore.spigot.hooks.CloudCoreSpigotDependency;
import systems.mythical.cloudcore.spigot.hooks.EssentialsXDependencyHandler;
import systems.mythical.cloudcore.spigot.hooks.ASkyBlockDependencyHandler;
import systems.mythical.cloudcore.spigot.hooks.EssentialsXHandler;
import systems.mythical.cloudcore.spigot.hooks.JobsDependencyHandler;
import systems.mythical.cloudcore.spigot.hooks.JobsHandler;
import systems.mythical.cloudcore.spigot.hooks.VoteHandler;
import systems.mythical.cloudcore.spigot.hooks.BedwarsHandler;
import systems.mythical.cloudcore.spigot.stats.UnifiedStatsTracker;

public class CloudCoreSpigot extends JavaPlugin {
    private CloudCore cloudCore;
    private DatabaseManager databaseManager;
    private WorkerManager workerManager;
    private ConsoleTaskScheduler consoleTaskScheduler;
    private MessageManager messageManager;
    private CloudSettings cloudSettings;
    private SettingsManager settingsManager;
    private StatsManager statsManager;
    private StatsBufferManager statsBufferManager;
    private DependencyManager dependencyManager;
    private EssentialsXHandler essentialsXHandler;
    private JobsHandler jobsHandler;
    private BedwarsHandler bedwarsHandler;
    private UnifiedStatsTracker statsTracker;

    @Override
    public void onEnable() {
        try {
            initDependencies();
            initConfig();
            initDatabase();
            initMessageManager();
            initSettings();
            initWorker();
            initTasks();
            registerCommands();
            initStatsManager();
            initStats();
            initStatsBuffer();
            initHandlers();
            initUnifiedStatsTracker();
            getLogger().info("CloudCore Spigot plugin has been enabled!");
        } catch (Exception e) {
            getLogger().severe("Failed to enable CloudCore Spigot: " + e.getMessage());
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        if (statsTracker != null) {
            statsTracker.stop();
        }
        if (databaseManager != null) {
            databaseManager.shutdown();
        }
        if (cloudCore != null) {
            cloudCore.shutdown();
        }

        getLogger().info("CloudCore Spigot plugin has been disabled!");
    }

    private void initConfig() {
        cloudCore = new CloudCore(getDataFolder(), getLogger(), true);
        getLogger().info("Config initialized.");
    }

    private void initDatabase() {
        databaseManager = new DatabaseManager(cloudCore.getConfig(), getLogger());
        getLogger().info("Database connection initialized.");
    }

    private void initStatsManager() {
        statsManager = new StatsManager(databaseManager, getLogger());
        getLogger().info("StatsManager initialized.");
    }

    private void initMessageManager() {
        messageManager = MessageManager.getInstance(databaseManager, getLogger());
        if (messageManager == null) {
            getLogger().severe("Failed to initialize MessageManager. Messages may not work correctly.");
        } else {
            getLogger().info("MessageManager initialized.");
        }
    }

    private void initSettings() {
        cloudSettings = CloudSettings.getInstance(databaseManager, getLogger());
        settingsManager = SettingsManager.getInstance(cloudSettings, getLogger());
        getLogger().info("SettingsManager initialized.");
    }

    private void initWorker() {
        workerManager = new WorkerManager(cloudCore.getConfig(), databaseManager, getLogger());
        workerManager.initialize();
        getLogger().info("WorkerManager initialized.");
    }

    private void initTasks() {
        consoleTaskScheduler = new ConsoleTaskScheduler(this);
        consoleTaskScheduler.start();
        getLogger().info("ConsoleTaskScheduler started.");
    }

    private void registerCommands() {
        CommonSettings.BooleanSetting enableConsoleCommand = new CommonSettings.BooleanSetting(
                Settings.ENABLE_CONSOLE_COMMAND, false);
        if (settingsManager.getValue(enableConsoleCommand)) {
            ConsoleCommand.initialize(databaseManager);
            getCommand("spigotconsole").setExecutor(new SpigotConsoleCommand(this));
            getCommand("scex").setExecutor(new SpigotConsoleCommand(this));
            getCommand("cex").setExecutor(new SpigotConsoleCommand(this));
            getLogger().info("Commands registered: /spigotconsole, /scex, /cex");
        } else {
            getLogger().info("Console command is disabled in settings. Not registering /spigotconsole, /scex, /cex.");
        }
    }

    private void initDependencies() {
        dependencyManager = new DependencyManager(this);
        dependencyManager.loadDependencies();
    }

    private void initStats() {
        getServer().getPluginManager().registerEvents(new StatsOnPlayerDeath(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerJoin(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerMineBlock(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerPlaceBlock(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerDamageTake(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerKill(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnPlayerLeave(this), this);
        getServer().getPluginManager().registerEvents(new StatsOnMobKill(this), this);
    }

    private void initStatsBuffer() {
        statsBufferManager = new StatsBufferManager(statsManager, this);
        statsBufferManager.startScheduledFlush(200L); // 10 seconds (20 ticks = 1 second)
        getLogger().info("StatsBufferManager initialized and scheduled.");
    }

    private void initHandlers() {
        // Initialize EssentialsX integration
        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.ESSENTIALSX)) {
            EssentialsXDependencyHandler handler = (EssentialsXDependencyHandler) dependencyManager
                    .getHandler(CloudCoreSpigotDependency.ESSENTIALSX);
            Essentials essentials = (Essentials) handler.get();
            essentialsXHandler = new EssentialsXHandler(essentials);
            getLogger().info("EssentialsX integration initialized.");
        } else {
            getLogger().info("EssentialsX not found, integration not initialized.");
        }

        // Initialize Jobs integration
        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.JOBS)) {
            JobsDependencyHandler handler = (JobsDependencyHandler) dependencyManager
                    .getHandler(CloudCoreSpigotDependency.JOBS);
            Jobs jobs = (Jobs) handler.get();
            jobsHandler = new JobsHandler(jobs);
            getLogger().info("Jobs integration initialized.");
        } else {
            getLogger().info("Jobs plugin not found, integration not initialized.");
        }

        // Initialize BedWars integration
        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.BEDWARS)) {
            try {
                bedwarsHandler = new BedwarsHandler();
                getLogger().info("BedWars integration initialized.");
            } catch (Exception e) {
                getLogger().warning("Failed to initialize BedWars integration: " + e.getMessage());
            }
        } else {
            getLogger().info("BedWars1058 plugin not found, integration not initialized.");
        }

        // Initialize Votifier integration
        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.VOTIFIER)) {
            try {
                getServer().getPluginManager().registerEvents(new VoteHandler(this), this);
                getLogger().info("Votifier integration initialized.");
            } catch (Exception e) {
                getLogger().warning("Failed to initialize Votifier integration: " + e.getMessage());
            }
        } else {
            getLogger().info("Votifier plugin not found, integration not initialized.");
        }
    }

    private void initUnifiedStatsTracker() {
        ASkyBlockAPI skyBlockAPI = null;
        Essentials essentials = null;

        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.ASKYBLOCK)) {
            @SuppressWarnings("unused")
			ASkyBlockDependencyHandler handler = (ASkyBlockDependencyHandler) dependencyManager
                    .getHandler(CloudCoreSpigotDependency.ASKYBLOCK);
            skyBlockAPI = ASkyBlockAPI.getInstance();
        }

        if (dependencyManager.isAvailable(CloudCoreSpigotDependency.ESSENTIALSX)) {
            EssentialsXDependencyHandler handler = (EssentialsXDependencyHandler) dependencyManager
                    .getHandler(CloudCoreSpigotDependency.ESSENTIALSX);
            essentials = (Essentials) handler.get();
        }

        statsTracker = new UnifiedStatsTracker(
                this,
                statsBufferManager,
                getWorkerName(),
                jobsHandler,
                essentialsXHandler,
                bedwarsHandler,
                essentials,
                skyBlockAPI);
        statsTracker.start();
        getLogger().info("UnifiedStatsTracker started for all integrations.");
    }

    public CloudCore getCloudCore() {
        return cloudCore;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public MessageManager getMessageManager() {
        return messageManager;
    }

    public StatsManager getStatsManager() {
        return statsManager;
    }

    public StatsBufferManager getStatsBufferManager() {
        return statsBufferManager;
    }

    public String getWorkerName() {
        return cloudCore.getConfig().getWorkerName();
    }

    public EssentialsXHandler getEssentialsXHandler() {
        return essentialsXHandler;
    }

    public JobsHandler getJobsHandler() {
        return jobsHandler;
    }

    public BedwarsHandler getBedwarsHandler() {
        return bedwarsHandler;
    }
}
