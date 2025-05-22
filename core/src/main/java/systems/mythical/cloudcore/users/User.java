package systems.mythical.cloudcore.users;

import java.time.LocalDateTime;
import java.util.UUID;

public class User {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String userVersion;
    private String userClientName;
    private String userConnectedServerName;
    private String email;
    private String avatar;
    private int credits;
    private String background;
    private UUID uuid;
    private String token;
    private int role;
    private String firstIp;
    private String lastIp;
    private boolean verified;
    private String supportPin;
    private boolean twoFactorEnabled;
    private String twoFactorKey;
    private boolean twoFactorBlocked;
    private String discordId;
    private Integer githubId;
    private String githubUsername;
    private String githubEmail;
    private boolean githubLinked;
    private String discordUsername;
    private String discordGlobalName;
    private String discordEmail;
    private boolean discordLinked;
    private boolean deleted;
    private boolean locked;
    private LocalDateTime lastSeen;
    private LocalDateTime firstSeen;

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getUserVersion() { return userVersion; }
    public void setUserVersion(String userVersion) { this.userVersion = userVersion; }

    public String getUserClientName() { return userClientName; }
    public void setUserClientName(String userClientName) { this.userClientName = userClientName; }

    public String getUserConnectedServerName() { return userConnectedServerName; }
    public void setUserConnectedServerName(String userConnectedServerName) { this.userConnectedServerName = userConnectedServerName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getBackground() { return background; }
    public void setBackground(String background) { this.background = background; }

    public UUID getUuid() { return uuid; }
    public void setUuid(UUID uuid) { this.uuid = uuid; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public int getRole() { return role; }
    public void setRole(int role) { this.role = role; }

    public String getFirstIp() { return firstIp; }
    public void setFirstIp(String firstIp) { this.firstIp = firstIp; }

    public String getLastIp() { return lastIp; }
    public void setLastIp(String lastIp) { this.lastIp = lastIp; }

    public boolean isVerified() { return verified; }
    public void setVerified(boolean verified) { this.verified = verified; }

    public String getSupportPin() { return supportPin; }
    public void setSupportPin(String supportPin) { this.supportPin = supportPin; }

    public boolean isTwoFactorEnabled() { return twoFactorEnabled; }
    public void setTwoFactorEnabled(boolean twoFactorEnabled) { this.twoFactorEnabled = twoFactorEnabled; }

    public String getTwoFactorKey() { return twoFactorKey; }
    public void setTwoFactorKey(String twoFactorKey) { this.twoFactorKey = twoFactorKey; }

    public boolean isTwoFactorBlocked() { return twoFactorBlocked; }
    public void setTwoFactorBlocked(boolean twoFactorBlocked) { this.twoFactorBlocked = twoFactorBlocked; }

    public String getDiscordId() { return discordId; }
    public void setDiscordId(String discordId) { this.discordId = discordId; }

    public Integer getGithubId() { return githubId; }
    public void setGithubId(Integer githubId) { this.githubId = githubId; }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getGithubEmail() { return githubEmail; }
    public void setGithubEmail(String githubEmail) { this.githubEmail = githubEmail; }

    public boolean isGithubLinked() { return githubLinked; }
    public void setGithubLinked(boolean githubLinked) { this.githubLinked = githubLinked; }

    public String getDiscordUsername() { return discordUsername; }
    public void setDiscordUsername(String discordUsername) { this.discordUsername = discordUsername; }

    public String getDiscordGlobalName() { return discordGlobalName; }
    public void setDiscordGlobalName(String discordGlobalName) { this.discordGlobalName = discordGlobalName; }

    public String getDiscordEmail() { return discordEmail; }
    public void setDiscordEmail(String discordEmail) { this.discordEmail = discordEmail; }

    public boolean isDiscordLinked() { return discordLinked; }
    public void setDiscordLinked(boolean discordLinked) { this.discordLinked = discordLinked; }

    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }

    public boolean isLocked() { return locked; }
    public void setLocked(boolean locked) { this.locked = locked; }

    public LocalDateTime getLastSeen() { return lastSeen; }
    public void setLastSeen(LocalDateTime lastSeen) { this.lastSeen = lastSeen; }

    public LocalDateTime getFirstSeen() { return firstSeen; }
    public void setFirstSeen(LocalDateTime firstSeen) { this.firstSeen = firstSeen; }
} 