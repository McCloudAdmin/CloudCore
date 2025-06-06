package systems.mythical.cloudcore.joinme;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class JoinMeTokenManager {
    private static JoinMeTokenManager instance;
    private final Map<String, JoinMeToken> activeTokens = new ConcurrentHashMap<>();

    private JoinMeTokenManager() {
    }

    public static JoinMeTokenManager getInstance() {
        if (instance == null) {
            instance = new JoinMeTokenManager();
        }
        return instance;
    }

    public String generateToken(String serverName, UUID creator) {
        String token = UUID.randomUUID().toString().substring(0, 8);
        activeTokens.put(token, new JoinMeToken(serverName, creator));
        return token;
    }

    public Optional<String> getServerFromToken(String token) {
        JoinMeToken joinToken = activeTokens.get(token);
        if (joinToken != null && joinToken.isValid()) {
            return Optional.of(joinToken.serverName);
        }
        return Optional.empty();
    }

    public boolean validateAndConsumeToken(String token, UUID player) {
        JoinMeToken joinToken = activeTokens.get(token);
        if (joinToken != null && joinToken.isValid()) {
            return true;
        }
        return false;
    }

    /**
     * Validates a token and executes a server connection if valid.
     * This method ensures atomic validation and connection handling.
     * 
     * @param token The token to validate
     * @param player The UUID of the player attempting to connect
     * @param connectionHandler A handler that will be called with the server name and player UUID if the token is valid
     * @return true if the token was valid and the connection handler was called
     */
    public boolean validateAndConnect(String token, UUID player, BiConsumer<String, UUID> connectionHandler) {
        JoinMeToken joinToken = activeTokens.get(token);
        if (joinToken != null && joinToken.isValid()) {
            connectionHandler.accept(joinToken.serverName, player);
            return true;
        }
        return false;
    }

    @SuppressWarnings("unused")
	public void cleanupExpiredTokens() {
        int before = activeTokens.size();
        activeTokens.entrySet().removeIf(entry -> !entry.getValue().isValid());
        int removed = before - activeTokens.size();
    }

    private static class JoinMeToken {
        final String serverName;
        final long expiry;
        @SuppressWarnings("unused")
		final UUID creator;

        JoinMeToken(String serverName, UUID creator) {
            this.serverName = serverName;
            this.creator = creator;
            this.expiry = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5); // 5 minute expiry
        }

        boolean isValid() {
            return System.currentTimeMillis() < expiry;
        }
    }
} 