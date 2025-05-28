package systems.mythical.cloudcore.chatlog;

import java.util.UUID;
import java.util.List;
import java.util.Map;

public class ChatLog {
    private final int id;
    private final UUID uuid;
    private final List<Map<String, Object>> messages;
    private final long createdAt;
    private final long updatedAt;
    private final boolean locked;
    private final boolean deleted;

    public ChatLog(int id, UUID uuid, List<Map<String, Object>> messages, long createdAt, long updatedAt, boolean locked, boolean deleted) {
        this.id = id;
        this.uuid = uuid;
        this.messages = messages;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.locked = locked;
        this.deleted = deleted;
    }

    public int getId() {
        return id;
    }

    public UUID getUuid() {
        return uuid;
    }

    public List<Map<String, Object>> getMessages() {
        return messages;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isDeleted() {
        return deleted;
    }
} 