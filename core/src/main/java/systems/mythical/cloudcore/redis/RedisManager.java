package systems.mythical.cloudcore.redis;

import com.google.gson.Gson;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisException;

import java.time.Duration;
import java.util.UUID;
import java.util.logging.Logger;

public class RedisManager {
    private static RedisManager instance;
    private final JedisPool jedisPool;
    private final Gson gson;
    private final Logger logger;
    private final String chatQueueKey = "mccloudadmin:chat:queue";
    private final String chatProcessingKey = "mccloudadmin:chat:processing";

    private RedisManager(String host, int port, String password, Logger logger) {
        this.logger = logger;
        this.gson = new Gson();
        
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(20);
        poolConfig.setMaxIdle(10);
        poolConfig.setMinIdle(5);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTime(Duration.ofSeconds(60));
        poolConfig.setTimeBetweenEvictionRuns(Duration.ofSeconds(30));
        
        if (password != null && !password.isEmpty()) {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000, password);
        } else {
            this.jedisPool = new JedisPool(poolConfig, host, port, 2000);
        }
        
        logger.info("Redis connection pool initialized for " + host + ":" + port);
    }

    public static RedisManager getInstance(String host, int port, String password, Logger logger) {
        if (instance == null) {
            instance = new RedisManager(host, port, password, logger);
        }
        return instance;
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RedisManager not initialized. Call getInstance(host, port, password, logger) first.");
        }
        return instance;
    }

    /**
     * Stores a chat message in Redis queue for processing
     */
    public void queueChatMessage(UUID playerUuid, String content, String server, long timestamp) {
        try (Jedis jedis = jedisPool.getResource()) {
            ChatMessage chatMessage = new ChatMessage(playerUuid, content, server, timestamp);
            String messageJson = gson.toJson(chatMessage);
            
            // Add to the processing queue
            jedis.lpush(chatQueueKey, messageJson);
            
            // Set a TTL on the queue to prevent memory issues (24 hours)
            jedis.expire(chatQueueKey, 86400);
            
            logger.fine("Queued chat message for player " + playerUuid + " from server " + server);
        } catch (JedisException e) {
            logger.severe("Failed to queue chat message in Redis: " + e.getMessage());
        }
    }

    /**
     * Retrieves and removes a chat message from the queue
     */
    public ChatMessage dequeueChatMessage() {
        try (Jedis jedis = jedisPool.getResource()) {
            String messageJson = jedis.brpop(0, chatQueueKey).get(1);
            return gson.fromJson(messageJson, ChatMessage.class);
        } catch (JedisException e) {
            logger.severe("Failed to dequeue chat message from Redis: " + e.getMessage());
            return null;
        }
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
        private final UUID playerUuid;
        private final String content;
        private final String server;
        private final long timestamp;

        public ChatMessage(UUID playerUuid, String content, String server, long timestamp) {
            this.playerUuid = playerUuid;
            this.content = content;
            this.server = server;
            this.timestamp = timestamp;
        }

        public UUID getPlayerUuid() { return playerUuid; }
        public String getContent() { return content; }
        public String getServer() { return server; }
        public long getTimestamp() { return timestamp; }
    }
} 