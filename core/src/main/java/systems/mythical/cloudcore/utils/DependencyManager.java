package systems.mythical.cloudcore.utils;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public class DependencyManager {
    private final Logger logger;
    private final Path pluginsFolder;
    private final Platform platform;
    private final DownloadManager downloadManager;
    private final Map<String, Dependency> dependencies;

    public enum Platform {
        VELOCITY,
        BUNGEECORD,
        SPIGOT;

        public String getFormattedName() {
            return name().toLowerCase();
        }
    }

    public static class Dependency {
        private final String name;
        private final String version;
        private final String url;
        private final String checksum;
        private final boolean required;
        private final Set<Platform> supportedPlatforms;

        private Dependency(Builder builder) {
            this.name = builder.name;
            this.version = builder.version;
            this.url = builder.url;
            this.checksum = builder.checksum;
            this.required = builder.required;
            this.supportedPlatforms = Collections.unmodifiableSet(new HashSet<>(builder.supportedPlatforms));
        }

        public static class Builder {
            private String name;
            private String version;
            private String url;
            private String checksum;
            private boolean required = true;
            private Set<Platform> supportedPlatforms = EnumSet.allOf(Platform.class);

            public Builder(String name) {
                this.name = name;
            }

            public Builder version(String version) {
                this.version = version;
                return this;
            }

            public Builder url(String url) {
                this.url = url;
                return this;
            }

            public Builder checksum(String checksum) {
                this.checksum = checksum;
                return this;
            }

            public Builder required(boolean required) {
                this.required = required;
                return this;
            }

            public Builder supportedPlatforms(Platform... platforms) {
                this.supportedPlatforms = EnumSet.noneOf(Platform.class);
                Collections.addAll(this.supportedPlatforms, platforms);
                return this;
            }

            public Dependency build() {
                return new Dependency(this);
            }
        }

        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getUrl() { return url; }
        public String getChecksum() { return checksum; }
        public boolean isRequired() { return required; }
        public Set<Platform> getSupportedPlatforms() { return supportedPlatforms; }
    }

    public DependencyManager(Logger logger, Path pluginsFolder, Platform platform) {
        this.logger = logger;
        this.pluginsFolder = pluginsFolder;
        this.platform = platform;
        this.downloadManager = new DownloadManager(logger);
        this.dependencies = new HashMap<>();
        initializeDependencies();
    }

    private void initializeDependencies() {
        // PacketEvents dependency
        dependencies.put("packetevents", new Dependency.Builder("packetevents")
            .version("2.8.0")
            .url("https://github.com/retrooper/packetevents/releases/download/v2.8.0/packetevents-%s-2.8.0.jar")
            .required(true)
            .supportedPlatforms(Platform.VELOCITY, Platform.BUNGEECORD)
            .build());

        // LuckPerms dependency with platform-specific URLs
        dependencies.put("LuckPerms", new Dependency.Builder("LuckPerms")
            .version("5.5.0")
            .url(platform == Platform.VELOCITY 
                ? "https://download.luckperms.net/1584/velocity/LuckPerms-Velocity-5.5.0.jar"
                : platform == Platform.BUNGEECORD 
                    ? "https://download.luckperms.net/1584/bungee/loader/LuckPerms-Bungee-5.5.0.jar" : "https://download.luckperms.net/1584/bukkit/loader/LuckPerms-Bukkit-5.5.0.jar")
            .required(true)
            .supportedPlatforms(Platform.VELOCITY, Platform.BUNGEECORD, Platform.SPIGOT)
            .build());

        // Add more dependencies here as needed
    }

    public CompletableFuture<Boolean> checkAndDownloadDependencies() {
        return CompletableFuture.supplyAsync(() -> {
            boolean success = true;
            for (Dependency dependency : dependencies.values()) {
                if (!dependency.getSupportedPlatforms().contains(platform)) {
                    continue;
                }

                String pluginName = dependency.getName();
                if (!isDependencyInstalled(pluginName) && dependency.isRequired()) {
                    success &= downloadDependency(dependency);
                }
            }
            return success;
        }).whenComplete((result, ex) -> {
            if (ex != null) {
                logger.severe("Error checking dependencies: " + ex.getMessage());
                ex.printStackTrace();
            }
            downloadManager.shutdown();
        });
    }

    private boolean isDependencyInstalled(String pluginName) {
        return pluginsFolder.resolve(pluginName + ".jar").toFile().exists();
    }

    private boolean downloadDependency(Dependency dependency) {
        try {
            String formattedUrl = String.format(dependency.getUrl(), platform.getFormattedName());
            Path targetPath = pluginsFolder.resolve(
                String.format("%s-%s-%s.jar", 
                    dependency.getName(),
                    platform.getFormattedName(),
                    dependency.getVersion()
                )
            );

            logger.info("Downloading " + dependency.getName() + " for " + platform.name() + "...");
            
            downloadManager.downloadFile(
                formattedUrl,
                targetPath.toString(),
                dependency.getChecksum(),
                progress -> logger.info(String.format(
                    "Downloading %s: %.1f%% (%.2f MB/s, %d seconds remaining)",
                    dependency.getName(),
                    progress.getProgress(),
                    progress.getSpeed() / (1024.0 * 1024.0),
                    progress.getEstimatedTimeRemaining()
                ))
            );

            logger.info("Successfully downloaded " + dependency.getName());
            return true;
        } catch (Exception e) {
            logger.severe("Failed to download " + dependency.getName() + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void addDependency(String name, Dependency dependency) {
        dependencies.put(name, dependency);
    }

    public Optional<Dependency> getDependency(String name) {
        return Optional.ofNullable(dependencies.get(name));
    }

    public Map<String, Dependency> getAllDependencies() {
        return Collections.unmodifiableMap(dependencies);
    }

    public void shutdown() {
        downloadManager.shutdown();
    }
} 