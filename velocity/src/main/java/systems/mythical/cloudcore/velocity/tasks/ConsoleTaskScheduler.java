package systems.mythical.cloudcore.velocity.tasks;

import com.velocitypowered.api.scheduler.ScheduledTask;
import systems.mythical.cloudcore.velocity.CloudCoreVelocity;
import systems.mythical.cloudcore.console.ConsoleTask;
import systems.mythical.cloudcore.console.ConsoleTaskManager;

import java.time.Duration;
import java.util.List;

public class ConsoleTaskScheduler {
    private final CloudCoreVelocity plugin;
    private final ConsoleTaskManager taskManager;
    private ScheduledTask task;

    public ConsoleTaskScheduler(CloudCoreVelocity plugin) {
        this.plugin = plugin;
        this.taskManager = ConsoleTaskManager.getInstance(plugin.getDatabaseManager(), plugin.getLogger(), "proxy");
    }

    public void start() {
        task = plugin.getServer().getScheduler()
            .buildTask(plugin, this::checkAndExecuteTasks)
            .repeat(Duration.ofMinutes(1))
            .schedule();
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
                // Execute the command in the proxy console
                plugin.getServer().getCommandManager().executeAsync(
                    plugin.getServer().getConsoleCommandSource(),
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