package systems.mythical.cloudcore.utils;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import java.util.concurrent.CompletableFuture;

public class DependencyManager {
    private final Path pluginsFolder;
    private final Platform platform;
    private final DownloadManager downloadManager;
    private final Map<String, Dependency> dependencies;
    private final CloudLogger cloudLogger = CloudLoggerFactory.get();

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
        this.pluginsFolder = pluginsFolder;
        this.platform = platform;
        this.downloadManager = new DownloadManager(logger);
        this.dependencies = new HashMap<>();
        initializeDependencies();
    }

    private void initializeDependencies() {
        // LuckPerms dependency with dynamic URL and version support.
        // The URL is now constructed using the version number in the path and filename,
        // and DependencyManager will always use the version provided below.
        // To update, just change the version in this method.
        final String luckPermsVersion = "5.5.17";
        final String baseDownloadUrl = "https://download.luckperms.net/";

        String platformPath;
        String fileName;

        switch (platform) {
            case VELOCITY:
                platformPath = String.format("%s/velocity", getLuckPermsBuildNumber(luckPermsVersion));
                fileName = String.format("LuckPerms-Velocity-%s.jar", luckPermsVersion);
                break;
            case BUNGEECORD:
                platformPath = String.format("%s/bungee/loader", getLuckPermsBuildNumber(luckPermsVersion));
                fileName = String.format("LuckPerms-Bungee-%s.jar", luckPermsVersion);
                break;
            case SPIGOT:
            default:
                platformPath = String.format("%s/bukkit/loader", getLuckPermsBuildNumber(luckPermsVersion));
                fileName = String.format("LuckPerms-Bukkit-%s.jar", luckPermsVersion);
                break;
        }

        String fullUrl = String.format("%s%s/%s", baseDownloadUrl, platformPath, fileName);

        dependencies.put("LuckPerms", new Dependency.Builder("LuckPerms")
            .version(luckPermsVersion)
            .url(fullUrl)
            .required(true)
            .supportedPlatforms(Platform.VELOCITY, Platform.BUNGEECORD, Platform.SPIGOT)
            .build());

        // Add more dependencies here as needed
    }

    /**
     * Returns the LuckPerms build number for the given version.
     * This mapping should be updated as new versions come out.
     * This is necessary because the download.luckperms.net URL is version/build-number specific.
     */
    private String getLuckPermsBuildNumber(String version) {
        // These build numbers must be kept up-to-date
        switch (version) {
            case "5.5.17": return "1606";
            case "5.5.0": return "1584";
            // Add more version <-> build mapping here as newer releases come out.
            // E.g.: case "5.4.109": return "1490";
            default:
                cloudLogger.warn("Unknown LuckPerms version: " + version + ". Using latest known build (1606). Update getLuckPermsBuildNumber()!");
                return "1606";
        }
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
                cloudLogger.error("Error checking dependencies: " + ex.getMessage());
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

            cloudLogger.info("Downloading " + dependency.getName() + " for " + platform.name() + "...");
            
            downloadManager.downloadFile(
                formattedUrl,
                targetPath.toString(),
                dependency.getChecksum(),
                progress -> cloudLogger.debug(String.format(
                    "Downloading %s: %.1f%% (%.2f MB/s, %d seconds remaining)",
                    dependency.getName(),
                    progress.getProgress(),
                    progress.getSpeed() / (1024.0 * 1024.0),
                    progress.getEstimatedTimeRemaining()
                ))
            );

            cloudLogger.info("Successfully downloaded " + dependency.getName());
            return true;
        } catch (Exception e) {
            cloudLogger.error("Failed to download " + dependency.getName() + ": " + e.getMessage());
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