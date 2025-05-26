package systems.mythical.cloudcore.users;

import java.time.LocalDateTime;

public class UserActivity {
    private int id;
    private String user;
    private String action;
    private String ipAddress;
    private boolean deleted;
    private boolean locked;
    private LocalDateTime date;
    private String context;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUser() { return user; }
    public void setUser(String user) { this.user = user; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
} 