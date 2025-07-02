package systems.mythical.cloudcore.spigot.tasks;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import systems.mythical.cloudcore.spigot.CloudCoreSpigot;
import systems.mythical.cloudcore.console.ConsoleTask;
import systems.mythical.cloudcore.console.ConsoleTaskManager;

import java.util.List;

public class ConsoleTaskScheduler {
    private final CloudCoreSpigot plugin;
    private final ConsoleTaskManager taskManager;
    private final String workerName;
    private BukkitTask task;

    public ConsoleTaskScheduler(CloudCoreSpigot plugin) {
        this.plugin = plugin;
        this.workerName = plugin.getCloudCore().getConfig().getWorkerName();
        this.taskManager = ConsoleTaskManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    public void start() {
        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkAndExecuteTasks, 0L, 1200L); // 1200 ticks = 1 min
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    private void checkAndExecuteTasks() {
        List<ConsoleTask> tasks = taskManager.getPendingTasks();
        for (ConsoleTask task : tasks) {
            try {
                // Check if this task is meant for this worker
                if (!workerName.equals(task.getExecuteOnServer())) {
                    continue; // Skip tasks not meant for this worker
                }

                // Execute the command in the server console
                Bukkit.getScheduler().runTask(plugin, () ->
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), task.getCommand())
                );

                // Mark the task as executed
                taskManager.markTaskAsExecuted(task.getId());

                plugin.getLogger().info("Executed console task #" + task.getId() + ": " + task.getCommand());
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to execute console task #" + task.getId() + ": " + e.getMessage());
            }
        }
    }
} 