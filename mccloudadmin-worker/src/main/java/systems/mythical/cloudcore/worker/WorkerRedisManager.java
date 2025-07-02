package systems.mythical.cloudcore.worker;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class WorkerRedisManager {
    private final JedisPool jedisPool;
    private final Gson gson;
    private final Logger logger;
    private final String chatQueueKey = "mccloudadmin:chat:queue";

    public WorkerRedisManager(WorkerConfig config, Logger logger) {
        this.logger = logger;
        this.gson = new Gson();
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(config.getRedisPoolSize());
        poolConfig.setMaxIdle(config.getRedisPoolSize() / 2);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        
        if (config.getRedisPassword() != null && !config.getRedisPassword().isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort(), 2000, config.getRedisPassword());
        } else {
            this.jedisPool = new JedisPool(poolConfig, config.getRedisHost(), config.getRedisPort(), 2000);
        }
        
        logger.info("Redis connection pool initialized for " + config.getRedisHost() + ":" + config.getRedisPort());
    }

    /**
     * Retrieves multiple chat messages from the queue
     */
    public List<ChatMessage> dequeueChatMessages(int batchSize, long timeoutMs) {
        List<ChatMessage> messages = new ArrayList<>();
        
        try (Jedis jedis = jedisPool.getResource()) {
            for (int i = 0; i < batchSize; i++) {
                try {
                    // Use BRPOP with timeout to avoid blocking indefinitely
                    List<String> result = jedis.brpop((int) (timeoutMs / 1000), chatQueueKey);
                    if (result != null && result.size() >= 2) {
                        String messageJson = result.get(1);
                        ChatMessage message = gson.fromJson(messageJson, ChatMessage.class);
                        messages.add(message);
                    } else {
                        // No more messages in queue
                        break;
                    }
                } catch (JedisException e) {
                    logger.warning("Error dequeuing message: " + e.getMessage());
                    break;
                }
            }
        } catch (JedisException e) {
            logger.severe("Failed to dequeue chat messages from Redis: " + e.getMessage());
        }
        
        if (!messages.isEmpty()) {
            logger.info("Dequeued " + messages.size() + " chat messages from Redis");
        }
        
        return messages;
    }

    /**
     * Gets the current queue size
     */
    public long getQueueSize() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.llen(chatQueueKey);
        } catch (JedisException e) {
            logger.severe("Failed to get queue size from Redis: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Checks if Redis is available
     */
    public boolean isAvailable() {
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.ping();
            return true;
        } catch (JedisException e) {
            logger.warning("Redis is not available: " + e.getMessage());
            return false;
        }
    }

    /**
     * Closes the Redis connection pool
     */
    public void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
            logger.info("Redis connection pool closed");
        }
    }

    /**
     * Chat message data class
     */
    public static class ChatMessage {
        private final java.util.UUID playerUuid;
        private final String content;
        private final String server;
        private final long timestamp;

        public ChatMessage(java.util.UUID playerUuid, String content, String server, long timestamp) {
            this.playerUuid = playerUuid;
            this.content = content;
            this.server = server;
            this.timestamp = timestamp;
        }

        public java.util.UUID getPlayerUuid() { return playerUuid; }
        public String getContent() { return content; }
        public String getServer() { return server; }
        public long getTimestamp() { return timestamp; }
    }
} 