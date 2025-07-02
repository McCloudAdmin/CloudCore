package systems.mythical.cloudcore.bungee.tasks;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import systems.mythical.cloudcore.bungee.CloudCoreBungee;
import systems.mythical.cloudcore.console.ConsoleTask;
import systems.mythical.cloudcore.console.ConsoleTaskManager;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class ConsoleTaskScheduler {
    private final CloudCoreBungee plugin;
    private final ConsoleTaskManager taskManager;
    private final String workerName;
    private ScheduledTask task;

    public ConsoleTaskScheduler(CloudCoreBungee plugin) {
        this.plugin = plugin;
        this.workerName = plugin.getCloudCore().getConfig().getWorkerName();
        this.taskManager = ConsoleTaskManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger());
    }

    public void start() {
        task = plugin.getProxy().getScheduler().schedule(plugin, this::checkAndExecuteTasks, 0, 1, TimeUnit.MINUTES);
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

                // Execute the command in the proxy console
                ProxyServer.getInstance().getPluginManager().dispatchCommand(
                    ProxyServer.getInstance().getConsole(),
                    task.getCommand()
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