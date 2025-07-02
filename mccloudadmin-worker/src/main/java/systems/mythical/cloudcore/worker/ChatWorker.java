package systems.mythical.cloudcore.worker;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ChatWorker {
    private static final Logger logger = Logger.getLogger(ChatWorker.class.getName());
    private final WorkerConfig config;
    private final WorkerRedisManager redisManager;
    private final WorkerDatabaseManager databaseManager;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ChatWorker(String configPath) throws Exception {
        logger.info("Starting MCCloudAdmin Chat Worker...");
        
        // Load configuration
        this.config = new WorkerConfig(configPath, logger);
        
        // Initialize Redis manager
        this.redisManager = new WorkerRedisManager(config, logger);
        
        // Initialize database manager
        this.databaseManager = new WorkerDatabaseManager(config, logger);
        
        // Ensure database table exists
        this.databaseManager.ensureTableExists();
        
        logger.info("Chat Worker initialized successfully");
    }

    public void start() {
        logger.info("Starting chat message processing...");
        
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutdown signal received, stopping worker...");
            running.set(false);
        }));

        while (running.get()) {
            try {
                // Check if Redis is available
                if (!redisManager.isAvailable()) {
                    logger.warning("Redis is not available, waiting before retry...");
                    Thread.sleep(config.getRetryDelayMs());
                    continue;
                }

                // Get current queue size for logging
                long queueSize = redisManager.getQueueSize();
                if (queueSize > 0) {
                    logger.info("Processing queue with " + queueSize + " messages");
                }

                // Dequeue messages in batches
                List<WorkerRedisManager.ChatMessage> messages = redisManager.dequeueChatMessages(
                    config.getBatchSize(), 
                    config.getPollIntervalMs()
                );

                if (!messages.isEmpty()) {
                    // Convert Redis messages to database messages
                    List<WorkerDatabaseManager.ChatMessage> dbMessages = messages.stream()
                        .map(msg -> new WorkerDatabaseManager.ChatMessage(
                            msg.getPlayerUuid(),
                            msg.getContent(),
                            msg.getServer(),
                            msg.getTimestamp()
                        ))
                        .toList();

                    // Insert messages into database
                    databaseManager.batchInsertChatMessages(dbMessages);
                } else {
                    // No messages to process, sleep for a short time
                    Thread.sleep(100);
                }

            } catch (InterruptedException e) {
                logger.info("Worker interrupted, shutting down...");
                break;
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Error processing chat messages", e);
                
                // Wait before retrying
                try {
                    Thread.sleep(config.getRetryDelayMs());
                } catch (InterruptedException ie) {
                    logger.info("Worker interrupted during error recovery, shutting down...");
                    break;
                }
            }
        }

        shutdown();
    }

    private void shutdown() {
        logger.info("Shutting down Chat Worker...");
        
        try {
            if (redisManager != null) {
                redisManager.close();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing Redis manager", e);
        }

        try {
            if (databaseManager != null) {
                databaseManager.close();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing database manager", e);
        }

        logger.info("Chat Worker shutdown complete");
    }

    public static void main(String[] args) {
        String configPath = "config.yml";
        
        if (args.length > 0) {
            configPath = args[0];
        }

        try {
            ChatWorker worker = new ChatWorker(configPath);
            worker.start();
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to start Chat Worker", e);
            System.exit(1);
        }
    }
} 